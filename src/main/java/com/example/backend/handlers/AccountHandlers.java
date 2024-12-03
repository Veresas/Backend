package com.example.backend.handlers;

import com.example.backend.DAO.UserRep;
import com.example.backend.models.AuthRequest;
import com.example.backend.models.Users;
import com.example.backend.utilites.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Component
public class AccountHandlers {

    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final UserRep userRepository;

    @Autowired // Добавлена аннотация @Autowired
    public AccountHandlers(UserRep userRepository, JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    public Mono<ServerResponse> login(ServerRequest request) {
        return request.bodyToMono(AuthRequest.class)
                .flatMap(authRequest ->
                        userRepository.findByName(authRequest.getName())
                                .flatMap(user -> {
                                    if (passwordEncoder.matches(authRequest.getPassword(), user.getPassword())) {
                                        String token = jwtUtil.generateToken(user.getName());
                                        Map<String, String> responseBody = new HashMap<>();
                                        responseBody.put("token", token);
                                        return ServerResponse.ok()
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .body(BodyInserters.fromValue(responseBody));
                                    } else {
                                        return ServerResponse.status(401)
                                                .bodyValue("Invalid credentials");
                                    }
                                })
                                .switchIfEmpty(ServerResponse.status(401)
                                        .bodyValue("User not found"))
                );
    }

    public Mono<ServerResponse> register(ServerRequest request) {
        return request.bodyToMono(AuthRequest.class)
                .flatMap(authRequest -> userRepository.findByName(authRequest.getName())
                        .flatMap(existingUser -> ServerResponse.status(409).bodyValue("User already exists"))
                        .switchIfEmpty(
                                userRepository.save(new Users(null,
                                                authRequest.getName(),
                                                passwordEncoder.encode(authRequest.getPassword()),
                                                null))
                                        .flatMap(savedUser -> ServerResponse.ok()
                                                .bodyValue("User registered successfully"))
                        )
                );
    }

    public Mono<ServerResponse> person(ServerRequest request) {
        String text = "Hello, world!";
        return ServerResponse
                .ok()
                .body(BodyInserters.fromValue(text));
    }
}
