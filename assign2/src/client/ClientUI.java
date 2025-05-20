package client;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ClientUI {
    private Socket socket;
    private BufferedReader serverIn;
    private PrintWriter serverOut;
    private Scanner userIn;
    private String username;
    private volatile boolean running = true;

    public void setUsername(String username) {
        this.username = username;
    }

    private String loadToken() {
        File file = new File("token_" + username + ".txt");
        if (!file.exists()) return null;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            return reader.readLine();
        } catch (IOException e) {
            return null;
        }
    }

    private void saveToken(String token) {
        try (PrintWriter writer = new PrintWriter(new FileWriter("token_" + username + ".txt"))) {
            writer.println(token);
        } catch (IOException e) {
            System.err.println("Could not save token.");
        }
    }

    public void start(String host, int port) {
        userIn = new Scanner(System.in);
        while (running) {
            try {
                connectAndChat(host, port);
            } catch (IOException e) {
                System.out.println("Connection lost. Attempting to reconnect...");
                try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
            }
        }
        close();
    }

    private void connectAndChat(String host, int port) throws IOException {
        socket = new Socket(host, port);
        serverIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        serverOut = new PrintWriter(socket.getOutputStream(), true);

        String token = loadToken();
        if (token != null) {
            serverOut.println("yes");
            serverOut.println(token);
        }
        else {
            serverOut.println("no");
        }

        Thread serverReader = new Thread(() -> {
            try {
                String line;
                while ((line = serverIn.readLine()) != null) {
                    System.out.println(line);
                    if (line.startsWith("Your session token: ")) {
                        String receivedToken = line.substring("Your session token: ".length()).trim();
                        saveToken(receivedToken);
                    }
                    if (line.contains("Invalid or expired token")) {
                        File f = new File("token_" + username + ".txt");
                        if (f.exists()) f.delete();
                    }
                }
            } catch (IOException e) {
                running = true;
                try { socket.close(); } catch (IOException ignored) {}
            }
        });
        serverReader.setDaemon(true);
        serverReader.start();

        while (running && !socket.isClosed()) {
            if (!userIn.hasNextLine()) break;
            String input = userIn.nextLine();
            serverOut.println(input);
            if (input.equalsIgnoreCase("/quit")) {
                System.out.println("Exiting...");
                running = false;
                break;
            }
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
        try (Scanner scanner = new Scanner(System.in)) {

            System.out.print("Enter server IP (default: localhost): ");
            String host = scanner.nextLine().trim();
            if (host.isEmpty()) host = "localhost";

            System.out.print("Enter port (default: 12345): ");
            String portStr = scanner.nextLine().trim();
            int port = portStr.isEmpty() ? 12345 : Integer.parseInt(portStr);

            System.out.print("Enter your username: ");
            String username = scanner.nextLine().trim();
            client.setUsername(username);

            client.start(host, port);
        }
    }
}