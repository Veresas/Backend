package com.example.backend.services;

import com.example.backend.models.RoomInMemori;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.*;

@Service
public class VideoWSService {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public Flux<WebSocketMessage> joinRoom(RoomInMemori room, WebSocketSession sender, String filmId) {

        List<WebSocketSession> sessions = new ArrayList<>(room.getParticipants().values());

        String jsonInfo = toJson("info", "Пользователь зашел", Map.of());
        String jsonMes = toJson("movieId", filmId, Map.of());

        WebSocketMessage initialPayload = sender.textMessage(jsonMes);

        return sender.send(Mono.just(initialPayload))
                .thenMany(mailing(sessions, sender, jsonInfo))
                .thenMany(Flux.empty());



    }

    private Mono<Void> mailing (List<WebSocketSession> sessions, WebSocketSession sender, String mes ){

        return Flux.fromIterable(sessions)
                .filter(recipient -> !recipient.equals(sender))
                .flatMap(recipient -> {
                    WebSocketMessage message = recipient.textMessage(mes);
                    return recipient.send(Mono.just(message));
                })
                .onErrorResume(e -> {
                    return sender.send(Mono.just(
                            sender.textMessage("Ошибка отправки: " + e.getMessage())
                    ));
                })
                .then();
    }

    public  Flux<WebSocketMessage> leaveRoom (RoomInMemori room, WebSocketSession sender){
        List<WebSocketSession> sessions = new ArrayList<>(room.getParticipants().values());
        String json = toJson("info", "Пользователь вышел", Map.of());

        return mailing(sessions, sender, json)
                    .thenMany(Flux.empty());

    }

    public Flux<WebSocketMessage> pause(RoomInMemori room, WebSocketSession sender, double time) {
        String json = toJson("pause", time, Map.of());
        List<WebSocketSession> sessions = new ArrayList<>(room.getParticipants().values());

        return mailing(sessions, sender, json).thenMany(Flux.empty());
    }

    public Flux<WebSocketMessage> play(RoomInMemori room, WebSocketSession sender, double time, long startAt) {
        String json = toJson("play", time, Map.of(
                "startAt", startAt
        ));
        List<WebSocketSession> sessions = new ArrayList<>(room.getParticipants().values());

        return mailing(sessions, sender, json).thenMany(Flux.empty());
    }

    public Flux<WebSocketMessage> seek(RoomInMemori room, WebSocketSession sender, double time) {
        String json = toJson("seek", time, Map.of());
        List<WebSocketSession> sessions = new ArrayList<>(room.getParticipants().values());
        return mailing(sessions, sender, json).thenMany(Flux.empty());
    }

    private String toJson(String type, Object value, Map<String, Object> options) {
        Map<String, Object> msg = new HashMap<>();
        msg.put("type", type);
        msg.put("value", value);
        msg.put("options", options);
        try {
            return objectMapper.writeValueAsString(msg);
        } catch (JsonProcessingException e) {
            return "{\"type\":\"error\",\"value\":\"serialization_failed\"}";
        }
    }
}

