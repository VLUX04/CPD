package server;

import javax.net.ssl.*;
import java.net.*;
import java.io.*;
import java.security.KeyStore;

public class ChatServer {
    public static void main(String[] args) {
        int port = 12345;

        try {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            FileInputStream keyStoreStream = new FileInputStream("serverkeystore.jks");
            keyStore.load(keyStoreStream, "cpdg162025".toCharArray());

            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(keyStore, "cpdg162025".toCharArray());

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), null, null);

            SSLServerSocketFactory ssf = sslContext.getServerSocketFactory();
            SSLServerSocket serverSocket = (SSLServerSocket) ssf.createServerSocket(port);

            AuthenticationManager authManager = new AuthenticationManager("users.txt");
            RoomManager roomManager = new RoomManager();
            TokenManager tokenManager = new TokenManager();

            System.out.println("ChatServer started on port " + port);

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

        } catch (Exception e) {
            System.err.println("Error starting the server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
