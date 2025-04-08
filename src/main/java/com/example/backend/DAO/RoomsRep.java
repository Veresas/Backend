package com.example.backend.DAO;

import com.example.backend.models.Rooms;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface RoomsRep extends ReactiveMongoRepository<Rooms, ObjectId> {

    Mono<Rooms> findByRoomId(String roomId);
}
