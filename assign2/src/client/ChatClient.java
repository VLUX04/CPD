package client;

import java.io.*;
import java.net.*;

public class ChatClient {
    public static void main(String[] args) {
        String host = "localhost";
        int port = 12345;

        try (Socket socket = new Socket(host, port);
             BufferedReader serverIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter serverOut = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader userIn = new BufferedReader(new InputStreamReader(System.in))) {

            Thread reader = new Thread(() -> {
                try {
                    String line;
                    while ((line = serverIn.readLine()) != null) {
                        System.out.println(line);
                    }
                } catch (IOException e) {
                    System.err.println("Connection closed.");
                }
            });

            reader.start();

            String userInput;
            while ((userInput = userIn.readLine()) != null) {
                serverOut.println(userInput);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
