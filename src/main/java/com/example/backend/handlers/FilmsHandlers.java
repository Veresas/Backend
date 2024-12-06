package com.example.backend.handlers;

import com.example.backend.models.Movies;
import com.example.backend.services.FilmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Component
public class FilmsHandlers {

    @Autowired
    private FilmsService filmsService;
    public Mono<ServerResponse> curentFilm (ServerRequest request){


        Flux<Movies> filmsList = filmsService.findAllFilms();

        return ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(filmsList, Movies.class);
    }

    public Mono<ServerResponse> filmList (ServerRequest request){
        return filmsService.findAllFilms()
                .collectList()
                .flatMap( films -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(films)
                )
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> getPoster (ServerRequest request){
        String filmId = request.pathVariable("id");
        Path posterPath = Paths.get("./sources/posters", filmId);
        Resource resource = new FileSystemResource(posterPath.toFile());
        return ServerResponse.ok()
                .contentType(MediaType.IMAGE_JPEG) // Или другой подходящий тип
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + posterPath + "\"")
                .body(Mono.just(resource), Resource.class);
    }
}
