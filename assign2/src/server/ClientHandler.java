package server;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final AuthenticationManager authManager;
    private final RoomManager roomManager;
    private final TokenManager tokenManager;
    private PrintWriter out;
    private String username;
    private Room currentRoom;

    public ClientHandler(Socket socket, AuthenticationManager authManager, RoomManager roomManager, TokenManager tokenManager) {
        this.socket = socket;
        this.authManager = authManager;
        this.roomManager = roomManager;
        this.tokenManager = tokenManager;
    }

    public String getUsername() {
        return username;
    }

    public void sendMessage(String message) {
        if (out != null) out.println(message);
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            out = new PrintWriter(socket.getOutputStream(), true);
            authenticateUser(in);

            String msg;
            while ((msg = in.readLine()) != null) {
                String msgTest = msg.trim();
                if (msgTest.isEmpty()) continue;

                tokenManager.saveUserRoom(username, currentRoom.getName());

                if (msg.equals("/rooms")) {
                    List<String> roomNames = roomManager.getRoomNames();
                    List<String> visibleRooms = new ArrayList<>();
                    for (String roomName : roomNames) {
                        Room room = roomManager.getRoom(roomName);
                        if (!room.isAIRoom() || (room.isAIRoom() && username.equals(room.getCreator()))) {
                            visibleRooms.add(roomName);
                        }
                    }
                    if (visibleRooms.isEmpty()) {
                        sendMessage("No rooms available. Use /join <room> or /createai <name> <prompt>.");
                    } else {
                        sendMessage("Available rooms:");
                        for (String room : visibleRooms) {
                            sendMessage("- " + room);
                        }
                    }
                    continue;
                }

                if (msg.startsWith("/join ")) {
                    String newRoomName = msg.substring(6).trim();
                    Room newRoom = roomManager.getOrCreateRoom(newRoomName);
                    if (newRoom.isAIRoom() && !username.equals(newRoom.getCreator())) {
                        sendMessage("You cannot join this AI room.");
                        continue;
                    }
                    currentRoom.leave(this);
                    newRoom.join(this);
                    currentRoom = newRoom;
                    tokenManager.saveUserRoom(username, newRoomName);
                    sendMessage("You joined room: " + newRoom.getName());
                    continue;
                }

                if (msg.startsWith("/createai ")) {
                    String[] parts = msg.split(" ", 3);
                    if (parts.length < 3) {
                        sendMessage("Usage: /createai <room_name> <prompt>");
                    } else {
                        String roomName = parts[1];
                        String prompt = parts[2];
                        Room aiRoom = roomManager.createAIRoom(roomName, prompt, username);
                        if (aiRoom == null) {
                            sendMessage("Room already exists.");
                        } else {
                            currentRoom.leave(this);
                            aiRoom.join(this);
                            currentRoom = aiRoom;
                            tokenManager.saveUserRoom(username, roomName);
                            sendMessage("AI room '" + roomName + "' created and joined.");
                            String systemPrompt = "You are a helpful chat bot for a chat room. When you answer, reply ONLY with the message text, WITHOUT any username or prefix. Do NOT start your response with \"Bot:\" or any username. The server adds your name (Bot) and the user's name as a prefix automatically to message, which is why in the chat history they appear. Here is the chat history and the latest user message, answer it: \n";
                            sendMessage("Bot is processing...");
                            String aiReply = AIHelper.getBotReply(systemPrompt + prompt, currentRoom.getFullChatHistory());
                            currentRoom.broadcast("Bot: " + aiReply);
                        }
                    }
                    continue;
                }

                if (msg.startsWith("/msg ")) {
                    String[] parts = msg.split(" ", 3);
                    if (parts.length >= 3) {
                        String target = parts[1];
                        String privateMsg = parts[2];
                        ClientHandler targetClient = roomManager.findUserGlobally(target);
                        if (targetClient != null) {
                            targetClient.sendMessage("[PM from " + username + "]: " + privateMsg);
                            sendMessage("[PM to " + target + "]: " + privateMsg);
                        } else {
                            sendMessage("User not found.");
                        }
                    }
                    continue;
                }

                currentRoom.broadcast(username + ": " + msg);
                if (currentRoom.isAIRoom()) {
                    sendMessage("Bot is processing...");
                    String systemPrompt = "You are a helpful chat bot for a chat room. When you answer, reply ONLY with the message text, WITHOUT any username or prefix. Do NOT start your response with \"Bot:\" or any username. The server adds your name (Bot) and the user's name as a prefix automatically to message, which is why in the chat history they appear. Here is the chat history and the latest user message, answer it: \n";
                    String aiReply = AIHelper.getBotReply(systemPrompt + currentRoom.getPrompt(), currentRoom.getFullChatHistory());
                    currentRoom.broadcast("Bot: " + aiReply);
                }
            }
        } catch (IOException e) {
            System.err.println("Error handling client: " + e.getMessage());
        } finally {
            try {
                if (currentRoom != null) {
                    currentRoom.leave(this);
                }
                if (currentRoom != null && username != null) {
                    tokenManager.saveUserRoom(username, currentRoom.getName());
                }
                socket.close();
            } catch (IOException e) {
                System.err.println("Error closing socket: " + e.getMessage());
            }
        }
    }

    private void authenticateUser(BufferedReader in) throws IOException {
        String mode = in.readLine();

        if ("yes".equalsIgnoreCase(mode)) {
            String token = in.readLine();
            String userFromToken = tokenManager.getUsernameFromToken(token);
            if (userFromToken != null) {
                this.username = userFromToken;
                sendMessage("Token authentication successful! Welcome back, " + username + ".");

                String roomName = tokenManager.getUserRoom(username);
                if (roomName != null && !roomName.isBlank()) {
                    currentRoom = roomManager.getOrCreateRoom(roomName);
                } else {
                    roomName = "Lobby";
                    currentRoom = roomManager.getOrCreateRoom("Lobby");
                }

                tokenManager.saveUserRoom(username, roomName); 

                if (currentRoom != null) {
                    String history = currentRoom.getFullChatHistory();
                    if (history != null && !history.isBlank()) {
                        sendMessage("---- Chat History ----\n" + history);
                    }
                    currentRoom.join(this);
                }
                return;
            } else {
                sendMessage("Invalid token. Switching to login...");
            }
        }

        sendMessage("Enter your username:");
        String userName = in.readLine();
        sendMessage("Enter your password:");
        String password = in.readLine();

        if (authManager.authenticate(userName, password)) {
            this.username = userName;
            sendMessage("Authentication successful!");
            String token = tokenManager.generateToken(userName);
            sendMessage("Your session token: " + token);

            currentRoom = roomManager.getOrCreateRoom("Lobby");
            tokenManager.saveUserRoom(username, "Lobby");

            if (currentRoom != null) {
                currentRoom.join(this);
                String history = currentRoom.getFullChatHistory();
                if (history != null && !history.isBlank()) {
                    sendMessage("---- Chat History ----\n" + history);
                }
            }
        } else {
            sendMessage("Authentication failed! Try again.");
            authenticateUser(in);
        }

        List<String> roomNames = roomManager.getRoomNames();
        List<String> visibleRooms = new ArrayList<>();
        for (String roomName : roomNames) {
            Room room = roomManager.getRoom(roomName);
            if (!room.isAIRoom() || (room.isAIRoom() && username.equals(room.getCreator()))) {
                visibleRooms.add(roomName);
            }
        }
        if (visibleRooms.isEmpty()) {
            sendMessage("No rooms available. Use /join <room> or /createai <name> <prompt>.");
        } else {
            sendMessage("Available rooms:");
            for (String room : visibleRooms) {
                sendMessage("- " + room);
            }
        }
    }
}
