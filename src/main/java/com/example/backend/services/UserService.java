package com.example.backend.services;

import com.example.backend.DAO.UserRep;
import com.example.backend.models.Users;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class UserService {

    private UserRep userRep;
    private final DatabaseClient databaseClient;

    @Autowired
    public UserService(UserRep userRep, DatabaseClient databaseClient) {
        this.userRep = userRep;
        this.databaseClient = databaseClient;
    }

    public Mono<Users> findByName(String name) {
        return userRep.findByName(name);
    }


    public Mono<Users> findById(String id) {
        return userRep.findById(UUID.fromString(id));
    }

    public Mono<Users> save(Users users) {
        return userRep.save(users);
    }

    public Mono<Void> findByIdAndReplace(Users users) {
        return userRep.findById(users.getId())
                .flatMap(existingUser -> userRep.save(users))
                .then();
    }

    public Mono<Users> update(Users users) {
        return userRep.findById(users.getId())
                .flatMap(existingUser -> {
                    existingUser.setEmail(users.getEmail());
                    existingUser.setName(users.getName());
                    existingUser.setMovies(users.getMovies());
                    existingUser.setPassword(users.getPassword());
                    existingUser.setRoleId(users.getRoleId());
                    return userRep.save(existingUser);
                });
    }

    public Mono<Void> deleteAll() {
        return userRep.deleteAll();
    }

    public Flux<Users> findAll() {
        return userRep.findAll();
    }
    public Mono<Void> addFilmToUsers(String idUser, String idFilm) {
        return databaseClient.sql("UPDATE movies SET user_id = :userId WHERE id = :movieId")
                .bind("userId", UUID.fromString(idUser))
                .bind("movieId", UUID.fromString(idFilm))
                .fetch()
                .rowsUpdated()
                .flatMap(updated -> {
                    if (updated == 0) {
                        return Mono.error(new IllegalArgumentException("Movie not found"));
                    }
                    return Mono.empty();
                });
    }
}
