package com.example.backend.services;

import com.example.backend.DAO.RoomsRep;
import com.example.backend.models.Rooms;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class RoomLifecycleService {

    private final RoomsRep roomsRep;

    public Mono<Void> markRoomActive(String roomId) {
        return roomsRep.findById(roomId)
                .doOnNext(room -> {
                    room.markUsed();
                })
                .flatMap(roomsRep::save)
                .then();
    }


    public Mono<Void> markRoomInActive(String roomId) {
        return roomsRep.findById(roomId)
                .doOnNext(room -> {
                    room.markInactive();
                })
                .flatMap(roomsRep::save)
                .then();
    }

    public Mono<Void> deactivateRoom(String roomId) {
        return roomsRep.findById(roomId)
                .doOnNext(Rooms::markInactive)
                .flatMap(roomsRep::save)
                .then();
    }
}
