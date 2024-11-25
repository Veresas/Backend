package com.example.backend.controllers;

import com.example.backend.handlers.FilmsHandlers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.*;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
@Configuration
public class MainWebController {

    @Bean
    public RouterFunction<ServerResponse> filmRout(FilmsHandlers filmsHandlers){
        return RouterFunctions
                .route(GET("/Films"), filmsHandlers::hello);
    }
}
