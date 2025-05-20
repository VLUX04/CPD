package client;

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ClientUI {
    private volatile Socket socket;
    private volatile BufferedReader serverIn;
    private volatile PrintWriter serverOut;
    private Scanner userIn;
    private volatile boolean running = true;
    private volatile boolean connected = false;
    private String username;
    private Thread readerThread;
    private Thread inputThread;

    public void setUsername(String username) {
        this.username = username;
    }

    private String getTokenFile() {
        return "token_" + username + ".txt";
    }

    private String loadToken() {
        try (BufferedReader reader = new BufferedReader(new FileReader(getTokenFile()))) {
            return reader.readLine();
        } catch (IOException e) {
            return null;
        }
    }

    private void saveToken(String token) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(getTokenFile()))) {
            writer.println(token);
        } catch (IOException e) {
            System.err.println("Could not save token.");
        }
    }

    private void deleteToken() {
        File file = new File(getTokenFile());
        if (file.exists()) {
            if (!file.delete()) {
                System.err.println("Could not delete token file.");
            }
        }
    }

    public void start(String host, int port) {
        userIn = new Scanner(System.in);

        // Start input thread once, independent of connection
        inputThread = new Thread(() -> {
            while (running) {
                if (connected && serverOut != null) {
                    if (userIn.hasNextLine()) {
                        String input = userIn.nextLine();
                        if (input.equalsIgnoreCase("/quit")) {
                            System.out.println("Exiting...");
                            running = false;
                            closeAll();
                            break;
                        }
                        serverOut.println(input);
                    } else {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException ignored) {}
                    }
                } else {
                    try {
                        Thread.sleep(300); // Wait while disconnected
                    } catch (InterruptedException ignored) {}
                }
            }
        });
        inputThread.setDaemon(true);
        inputThread.start();

        int reconnectAttempts = 0;

        while (running) {
            try {
                connect(host, port);
                connected = true;
                startReaderThread();

                reconnectAttempts = 0;

                // Wait here until connection lost
                while (running && connected) {
                    Thread.sleep(200);
                }
            } catch (IOException e) {
                System.out.println("Connection lost. Retrying in " + (3 + reconnectAttempts * 2) + " seconds...");
            } catch (InterruptedException e) {
                // Ignore interrupt
            }

            try {
                closeSocket();
                Thread.sleep(3000 + reconnectAttempts * 2000);
                reconnectAttempts++;
            } catch (InterruptedException | IOException ignored) {}
        }

        closeAll();
    }

    private void connect(String host, int port) throws IOException {
        socket = new Socket(host, port);
        serverIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        serverOut = new PrintWriter(socket.getOutputStream(), true);

        String token = loadToken();
        if (token != null) {
            serverOut.println("yes");
            serverOut.println(token);
            System.out.println("Attempting token-based login...");
        } else {
            serverOut.println("no");
        }

        System.out.println("Connected to the server.");
    }

    private void startReaderThread() {
        readerThread = new Thread(() -> {
            try {
                String line;
                while ((line = serverIn.readLine()) != null) {
                    System.out.println(line);
                    if (line.startsWith("Your session token: ")) {
                        String token = line.substring("Your session token: ".length()).trim();
                        saveToken(token);
                    }
                    if (line.contains("Invalid or expired token")) {
                        deleteToken();
                    }
                }
                System.out.println("Lost connection to server.");
            } catch (IOException e) {
                System.out.println("Lost connection to server.");
            } finally {
                connected = false;
            }
        });
        readerThread.setDaemon(true);
        readerThread.start();
    }

    private void closeSocket() throws IOException {
        if (socket != null && !socket.isClosed()) socket.close();
    }

    private void closeAll() {
        try {
            closeSocket();
        } catch (IOException ignored) {}
        if (userIn != null) userIn.close();
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
