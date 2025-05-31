package com.example.backend.services;

import com.example.backend.DAO.RoomsRep;
import com.example.backend.models.Rooms;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;

@Service
public class RoomsService {

    private RoomsRep roomsRep;
    private final DatabaseClient databaseClient;

    @Autowired
    public RoomsService(RoomsRep roomsRep, DatabaseClient databaseClient) {
        this.roomsRep = roomsRep;
        this.databaseClient = databaseClient;
    }

    public Flux<Rooms> findAllRooms() {
        return roomsRep.findAll();
    }

    public Flux<Rooms> findAllRoomsById(List<String> ids) {
        return roomsRep.findAllById(ids.stream().map(UUID::fromString).toList());
    }

    public Mono<Rooms> createRoom(Rooms rooms){
        return  roomsRep.save(rooms);
    }

    public Mono<Void> deleteAllRooms() {
        return roomsRep.deleteAll();
    }

    public Mono<Void> deleteRoomsById(List<String> ids) {
        return roomsRep.deleteAllById(ids.stream().map(UUID::fromString).toList());
    }

    public Mono<Void> updateMovieInRoom(String roomId, String movieId) {
        return roomsRep.findById(roomId)
                .flatMap(room -> {
                    room.setMovieId(UUID.fromString(movieId));
                    return roomsRep.save(room).then();
                });
    }

    public Mono<Void> updateOwnerInRoom(String roomId, String ownerId) {
        return roomsRep.findById(roomId)
                .flatMap(room -> {
                    room.setOwnerId(UUID.fromString(ownerId));
                    return roomsRep.save(room).then();
                });
    }

    public Mono<UUID> getFilmId(String roomId) {
        return roomsRep.findById(roomId)
                .map(Rooms::getMovieId);
    }

    public Mono<Void> updateRoomById(String id, Map<String, Object> updateData) {
        return roomsRep.findById(UUID.fromString(id))
                .flatMap(room -> {

                    if (updateData.containsKey("movieId")) {
                        room.setMovieId(UUID.fromString((String) updateData.get("movieId")));
                    }

                    if (updateData.containsKey("ownerId")) {
                        room.setOwnerId(UUID.fromString((String) updateData.get("ownerId")));
                    }

                    return roomsRep.save(room).then();
                });
    }


}
