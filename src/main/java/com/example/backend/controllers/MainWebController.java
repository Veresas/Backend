package com.example.backend.controllers;

import com.example.backend.handlers.AccountHandlers;
import com.example.backend.handlers.FilmsHandlers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.*;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;

@Configuration
public class MainWebController {

    @Bean
    public RouterFunction<ServerResponse> filmRout(FilmsHandlers filmsHandlers){
        return RouterFunctions
                .route(GET("/Films"), filmsHandlers::hello);
    }

    @Bean
    public RouterFunction<ServerResponse> AccRout(AccountHandlers accountHandlers){
        return RouterFunctions
                .route(POST("/acc/login"), accountHandlers::login)
                .andRoute(POST("/acc/reg"), accountHandlers::register)
                .andRoute(POST("/acc/person"), accountHandlers::person);
    }
}
