package com.example.backend.handlers;

import com.example.backend.services.VideoWSService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.SlicedByteBuf;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
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
        String roomId = extractRoomId(query);

        if (roomId == null) {
            return session.send(Mono.just(session.textMessage("Ошибка: неверный roomId")))
                    .then(session.close());
        }

        rooms.computeIfAbsent(roomId, k -> ConcurrentHashMap.newKeySet()).add(session);

        // Обработка закрытия соединения
        session.closeStatus()
                .doFinally(signal -> {
                    rooms.get(roomId).remove(session);
                })
                .subscribe();

        return session.send(
                session.receive()
                        .flatMap(message -> processMessage(session, message, roomId))
                        .onErrorResume(e -> handleErrors(session, e))
        );
    }

    private String extractRoomId(String query) {
        try {
            return Arrays.stream(query.split("&"))
                    .filter(param -> param.startsWith("roomId="))
                    .map(param -> param.split("=")[1])
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    private Publisher<WebSocketMessage> processMessage(
            WebSocketSession session,
            WebSocketMessage message, String roomId) {

        try {
            JsonNode json = objectMapper.readTree(message.getPayloadAsText());
            String action = json.path("action").asText("");

            switch (action) {
                case "join":
                    return videoWS.joinRoom(roomId, rooms, session)
                            .then(Mono.empty());
                case "leave":
                    return videoWS.leaveRoom(roomId, rooms, session)
                            .then(Mono.empty());
                case "test":
                    return videoWS.echo(session, json);
                default:
                    return Mono.just(session.textMessage("Неизвестное действие"));
            }
        } catch (JsonProcessingException e) {
            return Mono.just(session.textMessage("Ошибка парсинга JSON"));
        }
    }

    private Mono<WebSocketMessage> handleErrors(WebSocketSession session, Throwable e) {
        return Mono.just(session.textMessage("Ошибка: " + e.getMessage()));
    }
}
