package client;

import java.io.*;
import java.net.*;
import java.security.KeyStore;
import java.util.Scanner;

import javax.net.ssl.*;

public class ClientUI {
    private volatile Socket socket;
    private volatile BufferedReader serverIn;
    private volatile PrintWriter serverOut;
    private Scanner userIn;
    private volatile boolean running = true;
    private volatile boolean connected = false;
    private String username;

    public void setUsername(String username) {
        this.username = username;
    }

    private String getTokenFile() {
        return "helpers/token_" + username + ".txt";
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
            System.err.println("❌ Could not save token.");
        }
    }

    private void deleteToken() {
        File file = new File(getTokenFile());
        if (file.exists()) {
            if (!file.delete()) {
                System.err.println("❌ Could not delete token file.");
            }
        }
    }

    public void start(String host, int port) {
        userIn = new Scanner(System.in);

        Thread.startVirtualThread(() -> {
            while (running) {
                if (connected && serverOut != null) {
                    if (userIn.hasNextLine()) {
                        String input = userIn.nextLine();
                        if (input.equalsIgnoreCase("/quit")) {
                            System.out.println("👋 Exiting...");
                            running = false;
                            closeAll();
                            break;
                        }
                        if (input.equalsIgnoreCase("/createpriv")) {
                            System.out.print("🔒 Room name: ");
                            String roomName = userIn.nextLine().trim();
                            System.out.print("🗝️  Password: ");
                            String password = userIn.nextLine().trim();
                            serverOut.println("/createpriv " + roomName + " " + password);
                        } else if (input.equalsIgnoreCase("/joinpriv")) {
                            System.out.print("🔒 Room name: ");
                            String roomName = userIn.nextLine().trim();
                            System.out.print("🗝️  Password: ");
                            String password = userIn.nextLine().trim();
                            serverOut.println("/joinpriv " + roomName + " " + password);
                        } else {
                            serverOut.println(input);
                        }
                    } else {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException ignored) {}
                    }
                } else {
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException ignored) {}
                }
            }
        });

        int reconnectAttempts = 0;
        int maxRetries = 3;

        while (running && reconnectAttempts < maxRetries) {
            try {
                connect(host, port);
                connected = true;
                startReaderThread();

                reconnectAttempts = 0;

                while (running && connected) {
                    Thread.sleep(200);
                }
            } catch (IOException e) {
                System.out.println("❌ Connection lost. Retrying in " + (3 + reconnectAttempts * 2) + "s...");
            } catch (InterruptedException e) {
            }

            try {
                closeSocket();
                Thread.sleep(3000 + reconnectAttempts * 2000);
                reconnectAttempts++;
            } catch (InterruptedException | IOException ignored) {}
        }

        if (reconnectAttempts >= maxRetries) {
            System.out.println("❌ Failed to reconnect after " + maxRetries + " attempts. Exiting...");
        }

        closeAll();
    }

    private void connect(String host, int port) throws IOException {
        try {
            KeyStore trustStore = KeyStore.getInstance("PKCS12");
            FileInputStream trustStoreStream = new FileInputStream("serverkeystore.jks");
            trustStore.load(trustStoreStream, "cpdg162025".toCharArray());

            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(trustStore);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), null);

            SSLSocketFactory ssf = sslContext.getSocketFactory();
            socket = ssf.createSocket(host, port);
            serverIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            serverOut = new PrintWriter(socket.getOutputStream(), true);

        String token = loadToken();
        if (token != null) {
            serverOut.println("yes");
            serverOut.println(token);
            System.out.println("🔐 Attempting token-based login...");
        } else {
            serverOut.println("no");
        }

        System.out.println("✅ Connected to the server.");

        } catch (Exception e) {
            throw new IOException("SSL setup failed: " + e.getMessage(), e);
        }
    }

    private void startReaderThread() {
        Thread.startVirtualThread(() -> {
            try {
                String line;
                while ((line = serverIn.readLine()) != null) {
                    System.out.println(line);
                    if (line.startsWith("🔖 Your session token: ")) {
                        String token = line.substring("🔖 Your session token: ".length()).trim();
                        saveToken(token);
                    }
                    if (line.contains("❌ Invalid or expired token")) {
                        deleteToken();
                    }
                }
                System.out.println("⚠️  Lost connection to server.");
            } catch (IOException e) {
                System.out.println("⚠️  Lost connection to server.");
            } finally {
                connected = false;
            }
        });
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
            System.out.print("\033[H\033[2J");
            System.out.flush();

            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            System.out.println(" \033[1;36mDistributed Systems Assignment 2\033[0m");
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            System.out.println(" 👨‍💻  \033[1mDeveloped by:\033[0m");
            System.out.println("      🔹 Ana Carolina Coutinho");
            System.out.println("      🔹 Leonardo Ribeiro");
            System.out.println("      🔹 José Granja");
            System.out.println(" 🏫  \033[1mCourse:\033[0m Computação Paralela e Distribuída (CPD)");
            System.out.println(" 🧾  \033[1mTurma:\033[0m T04");
            System.out.println(" 👥  \033[1mGroup:\033[0m G16");
            System.out.println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ \n");
            System.out.print("🌐 Enter server IP (default: localhost): ");
            String host = scanner.nextLine().trim();
            if (host.isEmpty()) host = "localhost";

            System.out.print("🔌 Enter port (default: 12345): ");
            String portStr = scanner.nextLine().trim();
            int port = portStr.isEmpty() ? 12345 : Integer.parseInt(portStr);

            System.out.print("🙋 Enter your username: ");
            String username = scanner.nextLine().trim();
            client.setUsername(username);

            client.start(host, port);
        }
    }
}
