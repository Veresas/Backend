package com.example.backend.DAO;

import com.example.backend.models.Rooms;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;
import java.util.UUID;

public interface RoomsRep extends R2dbcRepository<Rooms, UUID> {
    Mono<Rooms> findById(String roomId);
}