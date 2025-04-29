package client;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ClientUI {
    private Socket socket;
    private BufferedReader serverIn;
    private PrintWriter serverOut;
    private Scanner userIn;

    public void start(String host, int port) {
        try {
            socket = new Socket(host, port);
            serverIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            serverOut = new PrintWriter(socket.getOutputStream(), true);
            userIn = new Scanner(System.in);

            // Thread to read messages from server
            new Thread(() -> {
                try {
                    String line;
                    while ((line = serverIn.readLine()) != null) {
                        System.out.println(line);
                    }
                } catch (IOException e) {
                    System.out.println("Disconnected from server.");
                }
            }).start();

            // Handle user input
            while (true) {
                String input = userIn.nextLine();
                serverOut.println(input);
                if (input.equalsIgnoreCase("/quit")) {
                    System.out.println("Exiting...");
                    break;
                }
            }

            close();

        } catch (IOException e) {
            System.err.println("Could not connect: " + e.getMessage());
        }
    }

    private void close() {
        try {
            if (socket != null) socket.close();
            if (userIn != null) userIn.close();
        } catch (IOException e) {
            System.err.println("Error during cleanup.");
        }
    }

    public static void main(String[] args) {
        ClientUI client = new ClientUI();
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter server IP (default: localhost): ");
        String host = scanner.nextLine();
        if (host.isEmpty()) host = "localhost";

        System.out.println("Enter port (default: 12345): ");
        String portStr = scanner.nextLine();
        int port = portStr.isEmpty() ? 12345 : Integer.parseInt(portStr);

        client.start(host, port);
    }
}
