package com.example.backend.handlers;

import com.example.backend.models.Movies;

import com.example.backend.services.FilmsService;
import com.example.backend.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.*;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
public class FilmsHandlers {

    private FilmsService filmsService;
    private UserService userService;

    @Autowired
    public FilmsHandlers(FilmsService filmsService, UserService userService){
        this.filmsService = filmsService;
        this.userService = userService;
    }

    public Mono<ServerResponse> filmList(ServerRequest request) {
        String userId = request.pathVariable("id");
        return userService.findById(userId)
                .flatMap(user -> {
                    List<String> filmsIds = user.getFilms();
                    if (filmsIds == null) {
                        filmsIds = Collections.emptyList();
                    }
                    return filmsService.findAllFilmsById(filmsIds)
                            .collectList()
                            .flatMap(films -> ServerResponse.ok()
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .bodyValue(films))
                            .switchIfEmpty(ServerResponse.notFound().build());
                })
                .switchIfEmpty(ServerResponse.notFound().build());

    }

    public Mono<ServerResponse> getPoster(ServerRequest request) {
        String filmId = request.pathVariable("id");
        Path posterPath = Paths.get("./sources/posters", filmId);
        Resource resource = new FileSystemResource(posterPath.toFile());
        return ServerResponse.ok()
                .contentType(MediaType.IMAGE_JPEG) // Или другой подходящий тип
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + posterPath + "\"")
                .body(Mono.just(resource), Resource.class);
    }

    public Mono<ServerResponse> addFile(ServerRequest request) {
        return request.multipartData().flatMap(parts -> {
            var fileName = UUID.randomUUID().toString();
            var title = ((FormFieldPart) parts.getFirst("title")).value();
            var photoPart = parts.getFirst("photo");
            var videoPart = parts.getFirst("video");
            var idPart = ((FormFieldPart) parts.getFirst("id")).value();


            if (idPart == null || idPart.isEmpty()) {
                return Mono.error(new IllegalArgumentException("idPart is missing or empty"));
            }
            Mono<Void> saveOnBd = filmsService.save(new Movies(null, title, fileName))
                    .flatMap(m -> {
                        System.out.println("ID пользователя: " + idPart);
                        return userService.addFilmToUsers(idPart, m.getId().toString());
                    })
                    .then();

            Mono<Void> savePhoto = saveFile(photoPart.content(), fileName + ".jpg", true);

            Mono<Void> saveVideo = saveFile(videoPart.content(), fileName + ".mp4", false);

            return Mono.when(saveOnBd, savePhoto, saveVideo)
                    .then(ServerResponse.ok().bodyValue("Files uploaded successfully"));
        });
    }

    private Mono<Void> saveFile(Flux<DataBuffer> dataBufferFlux, String fileName, Boolean isPhoto) {
        if (isPhoto) {
            fileName = "./sources/posters/" + fileName;
        } else {
            fileName = "./sources/films/" + fileName;
            System.out.println("Фильм распознан");
        }
        Path filePath = Paths.get(fileName);
        return DataBufferUtils.write(dataBufferFlux, filePath, StandardOpenOption.CREATE, StandardOpenOption.WRITE)
                .doOnError(e -> System.err.println("Error saving file: " + e.getMessage())) // Логируем ошибки
                .then();
    }

    public Mono<ServerResponse> getFilm(ServerRequest request){
        String filmId = request.pathVariable("id");
        Path filePath = Path.of("./sources/films/", filmId);
        Resource resource = new FileSystemResource(filePath.toFile());

        if (!resource.exists()) {
            return ServerResponse.notFound().build();
        }

        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM) // Или другой подходящий тип
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filmId + ".mp4" + "\"")
                .body(Mono.just(resource), Resource.class);
    }

    public Mono<ServerResponse> updateFilm(ServerRequest request) {
        return request.bodyToMono(Movies.class)
                .flatMap(film -> {
                    filmsService.updateFilm(film);
                    return ServerResponse.ok()
                            .build();
                });
    }

    public Mono<ServerResponse> delete(ServerRequest request){
        return filmsService.deleteAll()
                .flatMap(s -> ServerResponse.ok().build());
    }
}
