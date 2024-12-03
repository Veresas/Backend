package com.example.backend.DAO;

import com.example.backend.models.Users;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface UserRep extends ReactiveMongoRepository<Users, ObjectId> {

    Mono<Users> findByName(String name);
}
