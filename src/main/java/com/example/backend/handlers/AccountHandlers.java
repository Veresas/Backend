package com.example.backend.handlers;

import com.example.backend.models.Users;
import com.example.backend.services.UserService;
import com.example.backend.utilites.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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
    private final UserService userService;
    private final JavaMailSender emailSender;

    @Autowired
    public AccountHandlers(UserService userService, JwtUtil jwtUtil, PasswordEncoder passwordEncoder, JavaMailSender emailSender) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.emailSender = emailSender;
    }

    public Mono<ServerResponse> login(ServerRequest request) {
        return request.bodyToMono(Users.class)
                .flatMap(users ->
                        userService.findByName(users.getName())
                                .flatMap(user -> {
                                    if (passwordEncoder.matches(users.getPassword(), user.getPassword())) {
                                        String token = jwtUtil.generateToken(user.getRole(), user.getId().toString());
                                        Map<String, String> responseBody = new HashMap<>();
                                        responseBody.put("token", token);
                                        responseBody.put("userId", user.getId().toString());
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
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        return request.bodyToMono(Users.class)
                .flatMap(users -> {
                    String encodedPassword = passwordEncoder.encode(users.getPassword());
                    users.setPassword(encodedPassword);
                    users.setRole("GUEST");
                    return userService.findByName(users.getName())
                            .flatMap(existingUser -> ServerResponse.status(409).bodyValue("User already exists"))
                            .switchIfEmpty(userService.save(users)
                                    .flatMap(savedUser -> ServerResponse.ok().bodyValue("User registered successfully")));
                });
    }

    private void sendEmail(Users users){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(users.getEmail());
        message.setSubject("Успешная регистрация!");
        message.setText("Привет " + users.getName() + ",\nСпасибо за регистрацию");
        emailSender.send(message);
    }

    public Mono<ServerResponse> person(ServerRequest request) {

        return userService
                .findById(request.pathVariable("id"))
                .flatMap(users -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(users))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> updateUsers(ServerRequest request) {
        return request.bodyToMono(Users.class)
                .flatMap(user -> userService.update(user))
                .flatMap(u -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(u));

    }

    public Mono<ServerResponse> chek(ServerRequest request) {
        return ServerResponse
                .ok()
                .build();
    }

    public Mono<ServerResponse> delete(ServerRequest request){
        return userService.deleteAll()
                .flatMap(s -> ServerResponse.ok().build());
    }
}
