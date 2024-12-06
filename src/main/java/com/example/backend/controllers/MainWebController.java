package com.example.backend.controllers;

import com.example.backend.handlers.AccountHandlers;
import com.example.backend.handlers.FilmsHandlers;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.*;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
public class MainWebController {

    @Bean
    public RouterFunction<ServerResponse> filmRout(FilmsHandlers filmsHandlers){
        RequestPredicate filmsPredicate = path("/films");
        return RouterFunctions
                .nest(filmsPredicate,RouterFunctions
                        .route(GET("/filmList"), filmsHandlers::filmList)
                        .andRoute(GET("/p/{id}"), filmsHandlers::getPoster)
                        .andRoute(POST("/upload"), filmsHandlers::addFile)
                        .andRoute(GET("/v/{id}"), filmsHandlers::getFilm)
                );

    }

    @Bean
    public RouterFunction<ServerResponse> AccRout(AccountHandlers accountHandlers){
        RequestPredicate accPredicate = path("/acc");
        return RouterFunctions
                .nest(accPredicate, RouterFunctions
                        .route(POST("/login"), accountHandlers::login)
                        .andRoute(POST("/reg"), accountHandlers::register)
                        .andRoute(POST("/person"), accountHandlers::person)
                );
    }
}
