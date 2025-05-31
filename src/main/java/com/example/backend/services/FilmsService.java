package com.example.backend.services;

import com.example.backend.DAO.FilmRep;
import com.example.backend.models.Movies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Service
public class FilmsService {

    private FilmRep filmRep;
    private final DatabaseClient databaseClient;

    @Autowired
    public FilmsService(FilmRep filmRep, DatabaseClient databaseClient){
        this.filmRep = filmRep;
        this.databaseClient = databaseClient;
    }
    public Flux<Movies> findAllFilms() {
        return filmRep.findAll();
    }

    public Flux<Movies> findAllFilmsByUserId(UUID userId) {
        return databaseClient.sql("Select * movies WHERE id = :userId")
                .bind("userId", userId)
                .fetch()
                .all()
                .map(row -> (UUID) row.get("id"))
                .collectList()
                .flatMapMany(filmRep::findAllById);
    }

    public Flux<Movies> findAllFilmsIsPublic() {
        return filmRep.findByIsPublic(true);
    }

    public Mono<Movies> save(Movies movies) {
        return filmRep.save(movies);
    }

    public Mono<Void> findByIdAndReplace(Movies movies) {
        return filmRep.findById(movies.getId())
                .flatMap(existingFilm -> filmRep.save(movies))
                .then();
    }

    public Mono<Void> updateFilm(Movies film) {
        return filmRep.findById(film.getId())
                .flatMap(existingFilm -> {
                    existingFilm.setPoster(film.getPoster());
                    existingFilm.setTitle(film.getTitle());
                    return filmRep.save(existingFilm).then();
                });
    }

    public Mono<Void> deleteAll() {
        return filmRep.deleteAll();
    }
}
