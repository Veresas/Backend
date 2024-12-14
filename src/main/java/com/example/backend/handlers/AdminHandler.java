package com.example.backend.handlers;

import com.example.backend.models.Movies;
import com.example.backend.models.Users;
import com.example.backend.services.FilmsService;
import com.example.backend.services.UserService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Service
public class AdminHandler {

    private FilmsService filmsService;
    private UserService userService;

    @Autowired
    public AdminHandler(FilmsService filmsService, UserService userService) {
        this.filmsService = filmsService;
        this.userService = userService;
    }

    public Mono<ServerResponse> replaceFilm(ServerRequest request){
        return request.bodyToMono(Movies.class)
                .flatMap(film -> filmsService.findByIdAndReplace(film))
                .flatMap(res -> ServerResponse.ok()
                        .build());
    }



    public Mono<ServerResponse> replaceUsers(ServerRequest request){
        return request.bodyToMono(Users.class)
                .flatMap(users -> userService.findByIdAndReplace(users))
                .flatMap(u -> ServerResponse.ok()
                .build());

    }

    public Mono<ServerResponse> allUsers(ServerRequest request){
        return userService.findAll()
                .collectList()
                .flatMap(s -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(s)
                );
    }

    public Mono<ServerResponse> allFilms(ServerRequest request){
        return filmsService.findAllFilms()
                .collectList()
                .flatMap(s -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(s)
                );
    }

    public Mono<ServerResponse> test(ServerRequest request){
        return userService.addFilmToUsers("674f1e44081bcc264cef8f89", "67581ae7198667236a527bca")
                .flatMap(r -> ServerResponse.ok().build());
    }
}
