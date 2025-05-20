package server;

import java.net.*;
import java.io.*;

public class ChatServer {
 public static void main(String[] args) {
    int port = 12345;

    try (ServerSocket serverSocket = new ServerSocket(port)) {
        System.out.println("ChatServer running on port " + port);

        // Shared managers
        AuthenticationManager authManager = new AuthenticationManager("users.txt");
        RoomManager roomManager = new RoomManager();
        TokenManager tokenManager = new TokenManager();

        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();

                Thread.startVirtualThread(() -> {
                    try {
                        ClientHandler handler = new ClientHandler(clientSocket, authManager, roomManager, tokenManager);
                        handler.run();
                    } catch (Exception e) {
                        System.err.println("Error handling client: " + e.getMessage());
                    }
                });

            } catch (IOException e) {
                System.err.println("Error accepting client connection: " + e.getMessage());
            }
        }

    } catch (IOException e) {
        System.err.println("Error starting the server: " + e.getMessage());
    }
}

}
