package com.example.backend.handlers;

import com.example.backend.models.Movies;

import com.example.backend.models.RoomCreateRequest;
import com.example.backend.models.Rooms;
import com.example.backend.services.FilmsService;
import com.example.backend.services.RoomsService;
import com.example.backend.services.UserService;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.*;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.*;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;

@Component
public class FilmsHandlers {

    private FilmsService filmsService;
    private UserService userService;
    private RoomsService roomsService;

    @Autowired
    public FilmsHandlers(FilmsService filmsService, UserService userService, RoomsService roomsService){
        this.filmsService = filmsService;
        this.userService = userService;
        this.roomsService = roomsService;
    }

    public Mono<ServerResponse> filmList(ServerRequest request) {
        String userId = request.pathVariable("id");
        return userService.findById(userId)
                .flatMap(user -> filmsService.findAllFilmsByUserId(user.getId())
                        .collectList()
                        .flatMap(films -> ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(films))
                        .switchIfEmpty(ServerResponse.notFound().build()))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> filmListPublic(ServerRequest request) {
        return filmsService.findAllFilmsIsPublic()
                .collectList()
                .flatMap(films -> {
                    if (films.isEmpty()) {
                        return ServerResponse.ok()
                                .contentType(MediaType.APPLICATION_JSON)
                                .bodyValue(Collections.emptyList());
                    }
                    return ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(films);
                });
    }

    public Mono<ServerResponse> getPoster(ServerRequest request) {
        String filmId = request.pathVariable("id");
        Path posterPath = Paths.get("./sources/posters", filmId);
        Resource resource = null;
        try {
            resource = new UrlResource(posterPath.toUri());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
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
            Mono<Void> saveOnBd = filmsService.save(new Movies(null, title, fileName, UUID.fromString(idPart), false))
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

    @SneakyThrows
    public Mono<ServerResponse> getManifest(ServerRequest request) {
        String filmId = request.pathVariable("id");
        Path manifestPath = Path.of("./sources/films/", filmId, "/manifest.mpd");
        Resource resource = new UrlResource(manifestPath.toUri());

        if (!resource.exists()) {
            return ServerResponse.notFound().build();
        }

        return ServerResponse.ok()
                .contentType(MediaType.parseMediaType("application/dash+xml")) // Правильный MIME-тип
                .body(Mono.just(resource), Resource.class);
    }

    @SneakyThrows
    public Mono<ServerResponse> getSegment(ServerRequest request) {
        String filmId = request.pathVariable("id");
        String segmentName = request.pathVariable("segment");
        Path segmentPath = Path.of("./sources/films/", filmId,"/", segmentName);
        Resource resource = new UrlResource(segmentPath.toUri());

        if (!resource.exists()) {
            return ServerResponse.notFound().build();
        }

        return ServerResponse.ok()
                .contentType(MediaType.parseMediaType("video/mp4")) // MIME-тип для фрагментов
                .body(Mono.just(resource), Resource.class);
    }

    public Mono<ServerResponse> createRoom(ServerRequest request){
        return ReactiveSecurityContextHolder.getContext()
                .flatMap(auth -> {
                    String userId = auth.getAuthentication().getName();

                    return request.bodyToMono(RoomCreateRequest.class)
                            .flatMap(body -> {
                                Rooms room = new Rooms();
                                room.setOwnerId(UUID.fromString(userId));
                                room.setMovieId(body.getMovieId());
                                room.setPublic(body.isPublic());
                                room.markUsed();

                                return roomsService.createRoom(room)
                                        .flatMap(savedRoom -> ServerResponse.ok()
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .bodyValue(Collections.singletonMap("room_id", savedRoom.getId())));
                            });
                });

    }
}
