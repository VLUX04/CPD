package server;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final AuthenticationManager authManager;
    private final RoomManager roomManager;
    private PrintWriter out;
    private String username;
    private Room currentRoom;
    private final TokenManager tokenManager;


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
        if (out != null) {
            out.println(message);
        }
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            out = new PrintWriter(socket.getOutputStream(), true);

            // Authentication Process
            authenticateUser(in);

            // Start with the default room
            currentRoom = roomManager.getOrCreateRoom("Lobby");
            currentRoom.join(this);

            // Main loop for client communication
            String msg;
            while ((msg = in.readLine()) != null) {
                if (msg.startsWith("/join ")) {
                    String newRoomName = msg.substring(6).trim();
                    Room newRoom = roomManager.getOrCreateRoom(newRoomName);
                    currentRoom.leave(this);
                    newRoom.join(this);
                    currentRoom = newRoom;
                    sendMessage("You joined room: " + newRoom.getName());
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
                        } else {
                            sendMessage("User not found.");
                        }
                    }
                    continue;
                }

                // Broadcast normal message
                if (currentRoom.isAIRoom()) {
                    String aiReply = AIHelper.getBotReply(currentRoom.getPrompt(), currentRoom.getFullChatHistory());
                    currentRoom.broadcast("Bot: " + aiReply);
                } else {
                    currentRoom.broadcast(username + ": " + msg);
                }
            }
        } catch (IOException e) {
            System.err.println("Error handling client: " + e.getMessage());
        } finally {
            try {
                if (currentRoom != null) {
                    currentRoom.leave(this);
                }
                socket.close();
            } catch (IOException e) {
                System.err.println("Error closing socket: " + e.getMessage());
            }
        }
    }

   private void authenticateUser(BufferedReader in) throws IOException {
    while (true) {
        sendMessage("Do you have a token? (yes/no)");
        String reply = in.readLine();
        if ("yes".equalsIgnoreCase(reply)) {
            sendMessage("Enter your token:");
            String token = in.readLine();
            String userFromToken = tokenManager.getUsernameFromToken(token);
            if (userFromToken != null) {
                this.username = userFromToken;
                sendMessage("Token authentication successful! Welcome back, " + username + ".");
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
            return;
        } else {
            sendMessage("Authentication failed! Try again.");
        }
        }
    }

}
