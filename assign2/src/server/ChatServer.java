package server;

import java.net.*;
import java.io.*;

public class ChatServer {
    public static void main(String[] args) {
        int port = 12345;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("ChatServer running on port " + port);

            AuthenticationManager auth = new AuthenticationManager("users.txt");
            RoomManager roomManager = new RoomManager();

            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();

                    Thread.startVirtualThread(() -> {
                        try {
                            new ClientHandler(clientSocket, auth, roomManager).run();
                        } catch (Exception e) { // Catching a more generic exception
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
