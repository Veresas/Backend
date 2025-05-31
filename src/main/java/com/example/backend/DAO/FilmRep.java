package com.example.backend.DAO;

import com.example.backend.models.Movies;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface FilmRep extends R2dbcRepository<Movies, UUID> {
    Flux<Movies> findByIsPublic(boolean isPublic);
}