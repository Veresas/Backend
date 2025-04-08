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

    public Mono<Void> markRoomActive(String roomId, String userId) {
        return roomsRep.findByRoomId(roomId)
                .doOnNext(room -> {
                    room.markUsed();
                    room.addUser(userId);
                })
                .flatMap(roomsRep::save)
                .then();
    }

    public Mono<Void> removeUserAndPossiblyDeactivate(String roomId, String userId) {
        return roomsRep.findByRoomId(roomId)
                .doOnNext(room -> {
                    room.removeUser(userId);
                    if (room.isEmpty()) {
                        room.markInactive();
                    }
                })
                .flatMap(roomsRep::save)
                .then();
    }

    public Mono<Void> deactivateRoom(String roomId) {
        return roomsRep.findByRoomId(roomId)
                .doOnNext(Rooms::markInactive)
                .flatMap(roomsRep::save)
                .then();
    }
}
