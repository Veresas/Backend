package com.example.backend.services;

import com.example.backend.DAO.FilmRep;
import com.example.backend.models.Movies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class FilmsService {

    @Autowired
    private FilmRep filmRep;

    public Flux<Movies> findAllFilms(){
        return filmRep.findAll();
    }
}
