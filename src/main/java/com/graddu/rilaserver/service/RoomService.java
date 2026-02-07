package net.enjoy.springboot.registrationlogin.service;

import lombok.Data;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class RoomService {
    private final Map<String, Room> rooms = new ConcurrentHashMap<>();

    @Data
    public static class Room {
        private Set<Client> clients = ConcurrentHashMap.newKeySet();
        private int nextUserId = 0;
    }

    @Data
    public static class Client {
        private String sessionId;
        private String userId;
        private String role;
        private int assignedId;
        private WebSocketSession session;
    }

    public Room getOrCreateRoom(String roomId) {
        if (roomId == null) {
            throw new IllegalArgumentException("roomId cannot be null");
        }
        return rooms.computeIfAbsent(roomId, k -> new Room());
    }

    public boolean isUserInRoom(String roomId, String userId) {
        Room room = rooms.get(roomId);
        if (room == null) return false;
        return room.getClients().stream().anyMatch(client -> client.getUserId().equals(userId));
    }

    public void removeRoomIfEmpty(String roomId) {
        Room room = rooms.get(roomId);
        if (room != null && room.getClients().isEmpty()) {
            rooms.remove(roomId);
        }
    }

    /**
     * 获取用户在所有房间的所有在线客户端
     */
    public List<Client> getUserClients(String userId) {
        return rooms.values().stream()
                .flatMap(room -> room.getClients().stream())
                .filter(client -> client.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    /**
     * 获取用户在指定房间的客户端
     */
    public List<Client> getUserClientsInRoom(String roomId, String userId) {
        Room room = rooms.get(roomId);
        if (room == null) return List.of();
        
        return room.getClients().stream()
                .filter(client -> client.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    public Map<String, Room> getRooms() {
        return rooms;
    }
    
    /**
     * 获取指定房间的所有客户端
     */
    public List<Client> getRoomClients(String roomId) {
        Room room = rooms.get(roomId);
        if (room == null) {
            return List.of();
        }
        return room.getClients().stream().collect(Collectors.toList());
    }
} 