package server;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class Room {
    private final String name;
    private final boolean isAIRoom;
    private final String prompt;
    private final String creator;
    private final List<String> history = new ArrayList<>();
    private final Set<ClientHandler> clients = new HashSet<>();
    private final ReentrantLock lock = new ReentrantLock();

    public Room(String name) {
        this.name = name;
        this.isAIRoom = false;
        this.prompt = null;
        this.creator = null;
    }

    public Room(String name, String prompt, String creator) {
        this.name = name;
        this.isAIRoom = true;
        this.prompt = prompt;
        this.creator = creator;
    }

    public String getName() {
        return name;
    }

    public boolean isAIRoom() {
        return isAIRoom;
    }

    public String getPrompt() {
        return prompt;
    }

    public String getCreator() {
        return creator;
    }

    public void broadcast(String message) {
        lock.lock();
        try {
            history.add(message);
            for (ClientHandler client : clients) {
                client.sendMessage(message);
            }
        } finally {
            lock.unlock();
        }
    }

    public String getFullChatHistory() {
        lock.lock();
        try {
            return String.join("\n", history);
        } finally {
            lock.unlock();
        }
    }

    public void join(ClientHandler client) {
        lock.lock();
        try {
            if (clients.add(client)) {
                broadcast("[" + client.getUsername() + " enters the room]");
            }
        } finally {
            lock.unlock();
        }
    }

    public void leave(ClientHandler client) {
        lock.lock();
        try {
            if (clients.remove(client)) {
                broadcast("[" + client.getUsername() + " leaves the room]");
            }
        } finally {
            lock.unlock();
        }
    }

    public Set<ClientHandler> getClients() {
        lock.lock();
        try {
            return new HashSet<>(clients);
        } finally {
            lock.unlock();
        }
    }
}
