package com.example.backend.DAO;

import com.example.backend.models.Movies;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface FilmRep extends ReactiveMongoRepository<Movies, ObjectId> {

}
