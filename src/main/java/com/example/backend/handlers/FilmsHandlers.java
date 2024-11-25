package com.example.backend.handlers;

import com.example.backend.models.Movies;
import com.example.backend.services.FilmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class FilmsHandlers {

    @Autowired
    private FilmsService filmsService;
    public Mono<ServerResponse> hello (ServerRequest request){


        Flux<Movies> filmsList = filmsService.findAllFilms();

        return ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(filmsList, Movies.class);
    }
}
