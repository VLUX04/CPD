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
        try {
            socket = new Socket(host, port);
            serverIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            serverOut = new PrintWriter(socket.getOutputStream(), true);
            userIn = new Scanner(System.in);

            // Pergunta se quer usar token
            System.out.println("Do you want to login using a token? (yes/no)");
            String choice = userIn.nextLine().trim().toLowerCase();

            if (choice.equals("yes")) {
                String token = loadToken();
                if (token != null) {
                    serverOut.println("yes"); // modo token
                    serverOut.println(token);
                } else {
                    System.out.println("No saved token found for user: " + username);
                    serverOut.println("no"); // fallback para login manual
                }
            } else {
                serverOut.println("no"); // modo login manual
            }

            // Thread para ler mensagens do servidor
            Thread serverReader = new Thread(() -> {
                try {
                    String line;
                    while ((line = serverIn.readLine()) != null) {
                        System.out.println(line);

                        if (line.startsWith("Your session token: ")) {
                            String receivedToken = line.substring("Your session token: ".length()).trim();
                            saveToken(receivedToken);
                        }
                    }
                } catch (IOException e) {
                    System.out.println("Disconnected from server.");
                }
            });
            serverReader.start();

            // Thread principal: input do utilizador
            while (true) {
                if (!userIn.hasNextLine()) break;
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

        System.out.print("Enter your username: ");
        String username = scanner.nextLine().trim();
        client.setUsername(username);

        System.out.print("Enter server IP (default: localhost): ");
        String host = scanner.nextLine().trim();
        if (host.isEmpty()) host = "localhost";

        System.out.print("Enter port (default: 12345): ");
        String portStr = scanner.nextLine().trim();
        int port = portStr.isEmpty() ? 12345 : Integer.parseInt(portStr);

        client.start(host, port);
    }
}
