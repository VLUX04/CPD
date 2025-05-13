package server;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class TokenManager {
    private final Map<String, String> tokenToUser = new HashMap<>();
    private final Map<String, String> userToRoom = new HashMap<>();
    private final ReentrantLock lock = new ReentrantLock();

    public String generateToken(String username) {
        lock.lock();
        try {
            for (Map.Entry<String, String> entry : tokenToUser.entrySet()) {
                if (entry.getValue().equals(username)) {
                    return entry.getKey();
                }
            }
            String token = UUID.randomUUID().toString();
            tokenToUser.put(token, username);
            return token;
        } finally {
            lock.unlock();
        }
    }

    public String getUsernameFromToken(String token) {
        lock.lock();
        try {
            return tokenToUser.get(token);
        } finally {
            lock.unlock();
        }
    }

    public boolean isValidToken(String token) {
        lock.lock();
        try {
            return tokenToUser.containsKey(token);
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
