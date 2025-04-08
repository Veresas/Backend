package com.example.backend.handlers;

import com.example.backend.models.RoomInMemori;
import com.example.backend.services.RoomLifecycleService;
import com.example.backend.services.RoomsService;
import com.example.backend.services.VideoWSService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class MultiActionWebSocketHandler implements WebSocketHandler {
    private final ObjectMapper objectMapper;
    private final VideoWSService videoWS;
    private final RoomLifecycleService roomLifecycleService;
    private final RoomsService roomsService;

    private final Map<String, RoomInMemori> activeRooms = new ConcurrentHashMap<>();


    @Override
    public Mono<Void> handle(WebSocketSession session) {
        String query = session.getHandshakeInfo().getUri().getQuery();
        String roomId = extractParam(query, "roomId");
        String userId = extractParam(query, "userId");

        if (roomId == null || userId == null) {
            return session.send(Mono.just(session.textMessage("Ошибка: неверный roomId или userId")))
                    .then(session.close());
        }

        return roomLifecycleService.markRoomActive(roomId, userId)
                .then(Mono.fromRunnable(() -> {
                    activeRooms.computeIfAbsent(roomId, k -> new RoomInMemori(session));
                    RoomInMemori memRoom = activeRooms.get(roomId);
                    memRoom.addParticipant(userId, session);
                }))
                .thenMany(session.receive()
                        .flatMap(msg -> processMessage(session, msg, roomId))
                        .onErrorResume(e -> handleErrors(session, e))
                )
                .then()
                .doFinally(signal -> {
                    RoomInMemori memRoom = activeRooms.get(roomId);
                    if (memRoom != null) {
                        memRoom.removeParticipant(userId);
                        roomLifecycleService.removeUserAndPossiblyDeactivate(roomId, userId).subscribe();
                        if (memRoom.getParticipants().isEmpty()) {
                            activeRooms.remove(roomId);
                        }
                    }
                    videoWS.leaveRoom(memRoom, session).subscribe();
                });
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
            WebSocketMessage message, String roomId) {

        try {
            JsonNode json = objectMapper.readTree(message.getPayloadAsText());
            String action = json.path("action").asText("");
            RoomInMemori room = activeRooms.get(roomId);
            switch (action) {
                case "join":
                    return roomsService.getFilmId(roomId)
                            .flatMap(filmId -> {
                                return videoWS.joinRoom(room, session, filmId)
                                        .then(Mono.empty());
                            });

                case "pause":
                    double pauseTime = json.path("time").asDouble(0);
                    return videoWS.pause(room, session, pauseTime);

                case "play":
                    long startAt = json.path("startAt").asLong();
                    double currentTime = room.getParticipants().get(session.getId()) != null
                            ? room.getParticipants().get(session.getId()).getHandshakeInfo().getUri().getQuery().contains("userId") // just a presence check
                            ? json.path("time").asDouble(0) : 0
                            : 0;
                    return videoWS.play(room, session, currentTime, startAt);

                case "seek":
                    double seekTime = json.path("time").asDouble(0);
                    return videoWS.seek(room, session, seekTime);

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
