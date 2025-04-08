package com.example.backend.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.reactive.socket.WebSocketSession;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Data
public class RoomInMemori {
    private WebSocketSession owner;
    private Map<String, WebSocketSession> participants;
    private double currientTime = 0.0;

    public RoomInMemori(WebSocketSession owner) {
        this.owner = owner;
        this.participants = new ConcurrentHashMap<>();

        String ownerId = (String) owner.getAttributes().get("userId");
        if (ownerId != null) {
            this.participants.put(ownerId, owner);
        }
    }

    public void addParticipant(String userId, WebSocketSession session) {
        participants.put(userId, session);
    }

    public void removeParticipant(String userId) {
        participants.remove(userId);
    }

    public boolean isOwner(WebSocketSession session) {
        return session.getId().equals(owner.getId()); // Проверка по ID сессии
    }

    public boolean hasOwner() {
        String ownerId = (String) owner.getAttributes().get("userId");
        return ownerId != null && participants.containsKey(ownerId);
    }

    // Дополнительный метод для получения сессии по userId
    public WebSocketSession getParticipant(String userId) {
        return participants.get(userId);
    }

    // Дополнительный метод для проверки наличия участника
    public boolean hasParticipant(String userId) {
        return participants.containsKey(userId);
    }
}
