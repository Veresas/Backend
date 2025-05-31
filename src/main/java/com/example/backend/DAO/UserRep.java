package com.example.backend.DAO;

import com.example.backend.models.Users;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;
import java.util.UUID;

public interface UserRep extends R2dbcRepository<Users, UUID> {
    Mono<Users> findByName(String name);
}