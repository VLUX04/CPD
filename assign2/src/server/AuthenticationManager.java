package server;

import java.io.*;
import java.util.*;
import java.util.concurrent.locks.*;

public class AuthenticationManager {
    private final Map<String, String> users = new HashMap<>();
    private final Lock lock = new ReentrantLock();

    public AuthenticationManager(String userFilePath) throws IOException {
        loadUsers(userFilePath);
    }

    private void loadUsers(String path) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    users.put(parts[0], parts[1]);
                }
            }
        }
    }

    public boolean authenticate(String username, String password) {
        lock.lock();
        try {
            return users.containsKey(username) && users.get(username).equals(password);
        } finally {
            lock.unlock();
        }
    }
}
