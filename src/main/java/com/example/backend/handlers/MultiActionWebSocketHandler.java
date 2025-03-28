package com.example.backend.handlers;

import com.example.backend.services.VideoWSService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MultiActionWebSocketHandler implements WebSocketHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final VideoWSService videoWS;
    private final Map<String, Set<WebSocketSession>> rooms = new ConcurrentHashMap<>();


    @Autowired
    public MultiActionWebSocketHandler(VideoWSService videoWS) {
            this.videoWS = videoWS;
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        String query = session.getHandshakeInfo().getUri().getQuery();
        String roomId;

        if (query.contains("roomId=")) {
            try {
                roomId = Arrays.stream(query.split("&"))
                        .filter(param -> param.startsWith("roomId="))
                        .map(param -> param.split("=")[1])
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("roomId не найден"));
            } catch (Exception e) {
                return session.send(Mono.just(session.textMessage("Ошибка: неверный формат roomId")))
                        .then(session.close());
            }

            rooms.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet()).add(session);
        }

        return session.send(
                session.receive()
                        .flatMap(message -> Mono.fromCallable(() -> objectMapper.readTree(message.getPayloadAsText()))
                                .flatMap(json -> {
                                    JsonNode actionNode = json.get("action");
                                    if (actionNode == null || !actionNode.isTextual()) {
                                        return Mono.just(session.textMessage("Отсутствует или неверное поле 'action'"));
                                    }
                                    String action = actionNode.asText();
                                    switch (action) {
                                        case "join":
                                            return videoWS.joinRoom(session, json, rooms);
                                        case "test":
                                            return videoWS.echo(session, json);
                                        default:
                                            return Mono.just(session.textMessage("Неизвестное действие"));
                                    }
                                })
                                .onErrorResume(e -> {
                                    String errorMessage = (e instanceof JsonProcessingException)
                                            ? "Неверный JSON: " + e.getMessage()
                                            : "Внутренняя ошибка: " + e.getMessage();
                                    return Mono.just(session.textMessage(errorMessage));
                                })
                        )
        );
    }
}
