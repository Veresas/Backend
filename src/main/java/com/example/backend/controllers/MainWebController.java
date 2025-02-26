package com.example.backend.controllers;

import com.example.backend.handlers.AccountHandlers;
import com.example.backend.handlers.AdminHandler;
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
                        .route(GET("/filmList/{id}"), filmsHandlers::filmList)
                        .andRoute(GET("/p/{id}"), filmsHandlers::getPoster)
                        .andRoute(POST("/upload"), filmsHandlers::addFile)
                        .andRoute(GET("/v/{id}"), filmsHandlers::getFilm)
                        .andRoute(POST("/update"), filmsHandlers::updateFilm)
                );

    }

    @Bean
    public RouterFunction<ServerResponse> AccRout(AccountHandlers accountHandlers){
        RequestPredicate APredicate = path("/A");
        RequestPredicate accPredicate = path("/acc");
        return RouterFunctions
                .nest(APredicate, RouterFunctions
                        .route(POST("/login"), accountHandlers::login)
                        .andRoute(POST("/reg"), accountHandlers::register)
                )
                .andNest(accPredicate, RouterFunctions
                        .route(GET("/{id}"), accountHandlers::person)
                        .andRoute(POST("/update"), accountHandlers::updateUsers)
                )
                .andRoute(POST("/chek"), accountHandlers::chek);
    }

    @Bean
    public RouterFunction<ServerResponse> AdminRout(AccountHandlers accountHandlers,
                                                    FilmsHandlers filmsHandlers,
                                                    AdminHandler adminHandler){
        RequestPredicate filmPred = path("/film");
        RequestPredicate accPred = path("/acc");
        RequestPredicate adminPred = path("/adminqM");
        return RouterFunctions
                .nest(adminPred, RouterFunctions
                        .nest(filmPred, RouterFunctions
                                .route(POST("/update"), adminHandler::replaceFilm)
                                .andRoute(GET("/all"), adminHandler::allFilms)
                        )
                        .andNest(accPred, RouterFunctions
                                .route(POST("/update"), adminHandler::replaceUsers)
                                .andRoute(GET("/all"), adminHandler::allUsers)
                        )
                        .andRoute(GET("/test"), adminHandler::test)
                );
    }
}
