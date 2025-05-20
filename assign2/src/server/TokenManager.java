package server;

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
    private static final long TOKEN_VALIDITY_MS = 60 * 60 * 1000; // 1 hour

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
}