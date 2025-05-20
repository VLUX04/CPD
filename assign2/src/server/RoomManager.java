package server;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class RoomManager {
    private final Map<String, Room> rooms = new HashMap<>();
    private final ReentrantLock lock = new ReentrantLock();

    public Room getOrCreateRoom(String name) {
        lock.lock();
        try {
            return rooms.computeIfAbsent(name, k -> new Room(name, false, null));
        } finally {
            lock.unlock();
        }
    }

    public Room createAIRoom(String name, String prompt) {
        lock.lock();
        try {
            if (rooms.containsKey(name)) {
                return null; 
            }
            Room room = new Room(name, true, prompt);
            rooms.put(name, room);
            return room;
        } finally {
            lock.unlock();
        }
    }

    public ClientHandler findUserGlobally(String username) {
        lock.lock();
        try {
            for (Room room : rooms.values()) {
                for (ClientHandler client : room.getClients()) {
                    if (client.getUsername().equals(username)) {
                        return client;
                    }
                }
            }
            return null;
        } finally {
            lock.unlock();
        }
    }

    public List<String> getRoomNames() {
        lock.lock();
        try {
            return new ArrayList<>(rooms.keySet());
        } finally {
            lock.unlock();
        }
    }

    public Room getRoom(String name) {
        lock.lock();
        try {
            return rooms.get(name);
        } finally {
            lock.unlock();
        }
    }
}
