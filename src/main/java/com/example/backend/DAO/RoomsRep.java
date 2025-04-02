package com.example.backend.DAO;

import com.example.backend.models.Rooms;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface RoomsRep extends ReactiveMongoRepository<Rooms, ObjectId> {

}
