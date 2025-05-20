package server;

import java.io.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class TokenManager {
    private static class TokenInfo {
        String username;
        long expiresAt;
        TokenInfo(String username, long expiresAt) {
            this.username = username;
            this.expiresAt = expiresAt;
        }
    }

    private final Map<String, TokenInfo> tokenToInfo = new HashMap<>();
    private final Map<String, String> userToRoom = new HashMap<>();
    private final ReentrantLock lock = new ReentrantLock();
    private static final long TOKEN_VALIDITY_MS = 60 * 1000;

    private final File tokenFile = new File("helpers/tokens.txt");

    public TokenManager() {
        loadTokensFromFile();
    }

    public String generateToken(String username) {
        lock.lock();
        try {
            for (Map.Entry<String, TokenInfo> entry : tokenToInfo.entrySet()) {
                if (entry.getValue().username.equals(username) && entry.getValue().expiresAt > System.currentTimeMillis()) {
                    return entry.getKey();
                }
            }
            String token = UUID.randomUUID().toString();
            long expiresAt = System.currentTimeMillis() + TOKEN_VALIDITY_MS;
            tokenToInfo.put(token, new TokenInfo(username, expiresAt));
            saveTokensToFile();
            return token;
        } finally {
            lock.unlock();
        }
    }

    public String getUsernameFromToken(String token) {
        lock.lock();
        try {
            TokenInfo info = tokenToInfo.get(token);
            if (info != null && info.expiresAt > System.currentTimeMillis()) {
                return info.username;
            }
            return null;
        } finally {
            lock.unlock();
        }
    }

    public boolean isValidToken(String token) {
        lock.lock();
        try {
            TokenInfo info = tokenToInfo.get(token);
            return info != null && info.expiresAt > System.currentTimeMillis();
        } finally {
            lock.unlock();
        }
    }

    public void saveUserRoom(String username, String roomName) {
        lock.lock();
        try {
            userToRoom.put(username, roomName);
        } finally {
            lock.unlock();
        }
    }

    public String getUserRoom(String username) {
        lock.lock();
        try {
            return userToRoom.getOrDefault(username, "Lobby");
        } finally {
            lock.unlock();
        }
    }

    private void saveTokensToFile() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(tokenFile))) {
            for (Map.Entry<String, TokenInfo> e : tokenToInfo.entrySet()) {
                pw.println(e.getKey() + "|" + e.getValue().username + "|" + e.getValue().expiresAt);
            }
        } catch (IOException e) {
            System.err.println("Error saving tokens: " + e.getMessage());
        }
    }

    private void loadTokensFromFile() {
        if (!tokenFile.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(tokenFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length == 3) {
                    String token = parts[0];
                    String username = parts[1];
                    long expiresAt = Long.parseLong(parts[2]);
                    if (expiresAt > System.currentTimeMillis()) {
                        tokenToInfo.put(token, new TokenInfo(username, expiresAt));
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading tokens: " + e.getMessage());
        }
    }

}
