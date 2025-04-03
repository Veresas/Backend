package com.example.backend.handlers;

import com.example.backend.models.RoomInMemori;
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

import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MultiActionWebSocketHandler implements WebSocketHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final VideoWSService videoWS;
    private final Map<String, RoomInMemori> rooms = new ConcurrentHashMap<>();


    @Autowired
    public MultiActionWebSocketHandler(VideoWSService videoWS) {
            this.videoWS = videoWS;
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        String query = session.getHandshakeInfo().getUri().getQuery();
        String roomId = extractParam(query, "roomId");
        String userId = extractParam(query, "userId");

        if (roomId == null || userId == null) {
            return session.send(Mono.just(session.textMessage("Ошибка: неверный roomId или userId")))
                    .then(session.close());
        }

        session.getAttributes().put("userId", userId);
        RoomInMemori room = rooms.computeIfAbsent(roomId, k -> new RoomInMemori(session));
        room.addParticipant(userId, session);

        return session.send(
                session.receive()
                        .flatMap(message -> processMessage(session, message, room))
                        .onErrorResume(e -> handleErrors(session, e))
                        .doFinally(
                                signal -> {
                                    room.removeParticipant(userId);
                                    if (!room.getParticipants().isEmpty() && room.isOwner(session)) {
                                        // Владелец отключился, но есть другие участники
                                        Mono.delay(Duration.ofSeconds(15))
                                                .doOnNext(delay -> {
                                                    if (!room.hasOwner()) {
                                                        rooms.remove(roomId);
                                                        System.out.println("Комната " + roomId + " удалена из-за отсутствия владельца");
                                                    }
                                                })
                                                .subscribe();
                                    } else if (room.getParticipants().isEmpty()) {
                                        rooms.remove(roomId);
                                        System.out.println("Комната " + roomId + " удалена, так как пуста");
                                    }
                                })
        );

    }

    private String extractParam(String query, String paramName) {
        try {
            return Arrays.stream(query.split("&"))
                    .filter(param -> param.startsWith(paramName + "="))
                    .map(param -> param.split("=")[1])
                    .findFirst()
                    .orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    private Publisher<WebSocketMessage> processMessage(
            WebSocketSession session,
            WebSocketMessage message, RoomInMemori room) {

        try {
            JsonNode json = objectMapper.readTree(message.getPayloadAsText());
            String action = json.path("action").asText("");

            switch (action) {
                case "join":
                    return videoWS.joinRoom(room, session)
                            .then(Mono.empty());
                case "leave":
                    return videoWS.leaveRoom(room, session)
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
