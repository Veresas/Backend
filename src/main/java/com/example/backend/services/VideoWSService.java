package com.example.backend.services;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Set;

@Service
public class VideoWSService {

    public Mono<WebSocketMessage> joinRoom(WebSocketSession session, JsonNode json, Map<String, Set<WebSocketSession>> rooms) {
        JsonNode roomIdNode = json.get("roomId");
        if (roomIdNode == null || !roomIdNode.isTextual()) {
            return Mono.just(session.textMessage("Отсутствует или неверное поле 'roomId'"));
        }
        String roomId = roomIdNode.asText();
        return Mono.just(session.textMessage("Присоединился к комнате: " + roomId));
    }

    public Mono<WebSocketMessage> echo(WebSocketSession session, JsonNode json) {
        JsonNode action = json.get("action");
        if (action == null || !action.isTextual()) {
            return Mono.just(session.textMessage("Отсутствует или неверное поле 'roomId'"));
        }

        return Mono.just(session.textMessage("Действие " + action));
    }

}
