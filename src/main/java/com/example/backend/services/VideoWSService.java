package com.example.backend.services;

import com.example.backend.models.RoomInMemori;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.*;

@Service
public class VideoWSService {

    public Flux<WebSocketMessage> joinRoom(RoomInMemori room, WebSocketSession sender) {

        List<WebSocketSession> sessions = new ArrayList<>(room.getParticipants().values());
        String mes = "Пользователь примоединился";
        return mailing(sessions, sender, mes)
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
        String mes = "Пользователь вышел";
        return mailing(sessions, sender, mes)
                .thenMany(Flux.empty());
    }

    public Mono<WebSocketMessage> echo(WebSocketSession session, JsonNode json) {
        JsonNode action = json.get("action");
        if (action == null || !action.isTextual()) {
            return Mono.just(session.textMessage("Отсутствует или неверное поле 'roomId'"));
        }

        return Mono.just(session.textMessage("Действие " + action));
    }

}
