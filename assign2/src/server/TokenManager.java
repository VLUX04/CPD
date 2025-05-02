package server;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class TokenManager {
    private final Map<String, String> tokenToUser = new HashMap<>();
    private final ReentrantLock lock = new ReentrantLock();

    public String generateToken(String username) {
        lock.lock();
        try {
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
}
