package com.example.backend.DAO;

import com.example.backend.models.Movies;
import com.example.backend.models.Users;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface FilmRep extends ReactiveMongoRepository<Movies, ObjectId> {

}
