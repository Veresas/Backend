package com.example.backend.services;

import com.example.backend.DAO.FilmRep;
import com.example.backend.models.Movies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.data.mongodb.core.query.Query;
import java.util.List;

@Service
public class FilmsService {

    private FilmRep filmRep;
    private ReactiveMongoTemplate mongoTemplate;

    @Autowired
    public FilmsService(FilmRep filmRep, ReactiveMongoTemplate reactiveMongoTemplate){
        this.filmRep = filmRep;
        this.mongoTemplate = reactiveMongoTemplate;
    }
    public Flux<Movies> findAllFilms(){
        return filmRep.findAll();
    }

    public Flux<Movies> findAllFilmsById(List<String> ids){
        Query query = new Query();
        query.addCriteria(Criteria.where("id").in(ids));
        return mongoTemplate.find(query, Movies.class);
    }

    public Mono<Movies> save(Movies movies){
        return  filmRep.save(movies);
    }

    public Mono<Void> findByIdAndReplace(Movies movies){
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(movies.getId()));
        return mongoTemplate.replace(query,movies).then();
    }

    public Mono<Void> updateFilm(Movies film) {
        return mongoTemplate.findById(film.getId(), Movies.class)
                .flatMap(existingFilm -> {
                    existingFilm.setPoster(film.getPoster());
                    existingFilm.setTitle(film.getTitle());

                    return mongoTemplate.save(existingFilm).then();
                });
    }

    public Mono<Void> deleteAll(){
       return filmRep.deleteAll();
    }
}
