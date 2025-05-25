package server;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.net.Socket;
import java.util.Set;


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
            authenticateUser(in, false);

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
                            if (room.isPrivate()) {
                                visibleRooms.add("🔒 " + roomName);
                            } else {
                                visibleRooms.add("🟢 " + roomName);
                            }
                        }
                    }
                    sendMessage("📋 \033[1mAvailable Rooms:\033[0m");

                    if (visibleRooms.isEmpty()) {
                        sendMessage("❌ No rooms available. Use /join <room> or /createpriv <room>.");
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
                        sendMessage("❌ You cannot join this AI room.");
                        continue;
                    }
                    currentRoom.leave(this);
                    newRoom.join(this);
                    currentRoom = newRoom;
                    tokenManager.saveUserRoom(username, newRoomName);
                    sendMessage("✅ You have joined room: \033[1m" + newRoom.getName() + "\033[0m 🎉");
                    continue;
                }
                if (msg.equals("/logout")) {
                    tokenManager.deleteToken(username);   
                    if (currentRoom != null) {
                        currentRoom.leave(this);
                    }
                    sendMessage("🔒 You have been logged out.");
                    username = null;
                    currentRoom = null;

                    authenticateUser(in, true);
                    continue;
                }
                if (msg.equals("/help")) {
                    sendMessage("📚 \033[1mAvailable Commands\033[0m ━━━━━━━━━━━━━━");
                    sendMessage("🔹 \033[1m/help\033[0m           → Show this help menu");
                    sendMessage("🔹 \033[1m/whoami\033[0m         → Show your username and current room");
                    sendMessage("🔹 \033[1m/rooms\033[0m          → List all available rooms");
                    sendMessage("🔹 \033[1m/users\033[0m          → List users in the current room");
                    sendMessage("🔹 \033[1m/join <room>\033[0m    → Join or create a public room");
                    sendMessage("🔹 \033[1m/joinpriv\033[0m       → Join a private room (via prompt)");
                    sendMessage("🔹 \033[1m/createpriv\033[0m     → Create a private room");
                    sendMessage("🔹 \033[1m/createai <r> <p>\033[0m → Create AI chat room with prompt");
                    sendMessage("🔹 \033[1m/msg <u> <msg>\033[0m  → Send a private message");
                    sendMessage("🔹 \033[1m/leave\033[0m          → Leave current room to Lobby");
                    sendMessage("🔹 \033[1m/logout\033[0m         → Log out and delete session token");
                    sendMessage("🔹 \033[1m/quit\033[0m           → Exit the chat");
                    sendMessage("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                    continue;
                }


                if (msg.equals("/users")) {
                    Set<ClientHandler> users = currentRoom.getClients();
                    if (users.isEmpty()) {
                        sendMessage("No users in this room.");
                    } else {
                        sendMessage("👥 \033[1mUsers in '" + currentRoom.getName() + "':\033[0m");
                        for (ClientHandler user : users) {
                            sendMessage("- " + user.getUsername());
                        }
                    }
                    continue;
                }
                if (msg.equals("/leave")) {
                    currentRoom.leave(this);
                    currentRoom = roomManager.getOrCreateRoom("Lobby");
                    currentRoom.join(this);
                    tokenManager.saveUserRoom(username, "Lobby");
                    sendMessage("↩️  You left the room and joined the \033[1mLobby\033[0m.");
                    continue;
                }



                if (msg.equals("/whoami")) {
                    sendMessage("🧍 \033[1mUsername:\033[0m " + username);
                    sendMessage("🗂️  \033[1mCurrent room:\033[0m " + currentRoom.getName());
                    sendMessage("🔐 \033[1mToken status:\033[0m " + (tokenManager.hasToken(username) ? "active ✅" : "none ❌"));
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
                            sendMessage("❌ Room already exists.");
                        } else {
                            currentRoom.leave(this);
                            aiRoom.join(this);
                            currentRoom = aiRoom;
                            tokenManager.saveUserRoom(username, roomName);
                            sendMessage("AI room '" + roomName + "' created and joined.");
                            String systemPrompt = "You are a helpful chat bot for a chat room. When you answer, reply ONLY with the message text, WITHOUT any username or prefix. Do NOT start your response with \"Bot:\" or any username. The server adds your name (Bot) and the user's name as a prefix automatically to message, which is why in the chat history they appear. Here is the chat history and the latest user message, answer it: \n";
                            String aiReply = AIHelper.getBotReply(systemPrompt + prompt, currentRoom.getFullChatHistory());
                            currentRoom.broadcast("Bot: " + aiReply);
                        }
                    }
                    continue;
                }

                if (msg.startsWith("/createpriv ")) {
                    String[] parts = msg.split(" ", 3);
                    if (parts.length < 3) {
                        sendMessage("Usage: /createpriv <room_name> <password>");
                    } else {
                        String roomName = parts[1];
                        String password = parts[2];
                        Room privRoom = roomManager.createPrivateRoom(roomName, password, username);
                        if (privRoom == null) {
                            sendMessage("❌ Room already exists.");
                        } else {
                            currentRoom.leave(this);
                            privRoom.join(this);
                            currentRoom = privRoom;
                            tokenManager.saveUserRoom(username, roomName);
                            sendMessage("🔒 Private room '\033[1m" + roomName + "\033[0m' created and joined.");
                        }
                    }
                    continue;
                }

                if (msg.startsWith("/joinpriv ")) {
                    String[] parts = msg.split(" ", 3);
                    if (parts.length < 3) {
                        sendMessage("Usage: /joinpriv <room_name> <password>");
                    } else {
                        String roomName = parts[1];
                        String password = parts[2];

                        Room targetRoom = roomManager.getRoomIfPasswordMatches(roomName, password);
                        if (targetRoom == null) {
                            sendMessage("❌ Wrong password or room doesn't exist.");
                        } else {
                            currentRoom.leave(this);
                            targetRoom.join(this);
                            currentRoom = targetRoom;
                            tokenManager.saveUserRoom(username, roomName);
                            sendMessage("✅ You joined private room: \033[1m" + roomName + "\033[0m");
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
                            targetClient.sendMessage("📩 [PM from " + username + "]: " + privateMsg);
                            sendMessage("📤 [PM to " + target + "]: " + privateMsg);
                        } else {
                            sendMessage("❌ User not found.");
                        }
                    }
                    continue;
                }

                currentRoom.broadcast(username + ": " + msg);
                if (currentRoom.isAIRoom()) {
                    String systemPrompt = "You are a helpful chat bot for a chat room. When you answer, reply ONLY with the message text, WITHOUT any username or prefix. Do NOT start your response with \"Bot:\" or any username. The server adds your name (Bot) and the user's name as a prefix automatically to message, which is why in the chat history they appear. Here is the chat history and the latest user message, answer it: \n";
                    String aiReply = AIHelper.getBotReply(systemPrompt + currentRoom.getPrompt(), currentRoom.getFullChatHistory());
                    currentRoom.broadcast("\033[1mBot\033[0m: " + aiReply);
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

    private void authenticateUser(BufferedReader in, boolean firstAuth) throws IOException {
        String mode = "";
        if(!firstAuth) mode = in.readLine();

        if ("yes".equalsIgnoreCase(mode)) {
            String token = in.readLine();
            String userFromToken = tokenManager.getUsernameFromToken(token);
            if (userFromToken != null) {
                this.username = userFromToken;
                sendMessage("🔓 Token authentication successful! Welcome back, \033[1m" + username + "\033[0m.");

                String roomName = tokenManager.getUserRoom(username);
                if (roomName != null && !roomName.isBlank()) {
                    currentRoom = roomManager.getOrCreateRoom(roomName);
                } else {
                    currentRoom = roomManager.getRoom("Lobby");
                }

                tokenManager.saveUserRoom(username, roomName); 

                if (currentRoom != null) {
                    String history = currentRoom.getFullChatHistory();
                    if (history != null && !history.isBlank()) {
                        sendMessage("📜 \033[1mChat History:\033[0m\n" + history);
                    }
                    currentRoom.join(this);
                }
                return;
            } else {
                sendMessage("❌ Invalid token. Switching to manual login...");
            }
        }
        
        while(true){
            sendMessage("=============== Please authenticate: ===============");
            sendMessage("🔐 Enter your username:");
            String userName = in.readLine();
            sendMessage("🔑 Enter your password:");
            String password = in.readLine();

            if (authManager.authenticate(userName, password)) {
                this.username = userName;
                sendMessage("✅ Authentication successful! Welcome, \033[1m" + username + "\033[0m.");
                String token = tokenManager.generateToken(userName);
                sendMessage("🔖 Your session token: " + token);

                if (roomManager.getRoom("Lobby") == null) {
                    currentRoom = roomManager.getOrCreateRoom("Lobby");
                } else {
                    currentRoom = roomManager.getRoom("Lobby");
                }
                
                tokenManager.saveUserRoom(username, "Lobby");

                if (currentRoom != null) {
                    currentRoom.join(this);
                    String history = currentRoom.getFullChatHistory();
                    if (history != null && !history.isBlank()) {
                        sendMessage("📜 \033[1mChat History:\033[0m\n" + history);
                    }
                }
                break;
            } else {
                sendMessage("❌ Authentication failed. Try again.");
            }
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
            sendMessage("❗ No rooms available. Use /join <room> or /createai <name> <prompt>.");
        } else {
            sendMessage("📋 Available rooms:");
            for (String room : visibleRooms) {
                sendMessage("- " + room);
            }
        }
    }
}
