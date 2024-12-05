package com.example.backend.utilites;

import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

@Component
public class GlobalExceptionHandler implements WebExceptionHandler {

    @Override
    public Mono<Void> handle(org.springframework.web.server.ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Access-Control-Allow-Origin", "*"); // Добавляем CORS-заголовок
        response.getHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");
        return response.setComplete();
    }
}
