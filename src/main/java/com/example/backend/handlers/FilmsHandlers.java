package com.example.backend.handlers;

import com.example.backend.models.Movies;

import com.example.backend.services.FilmsService;
import com.example.backend.services.UserService;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.*;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.*;
import org.springframework.http.codec.multipart.FormFieldPart;
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

    @SneakyThrows
    public Mono<ServerResponse> getFilm(ServerRequest request){
        String filmId = request.pathVariable("id");
        Path filePath = Path.of("./sources/films/", filmId);
        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists()) {
            return ServerResponse.notFound().build();
        }

        return request.headers().header(HttpHeaders.RANGE).stream()
                        .findFirst()
                                .map(rageHeader -> {
                                    long rangeStart = 0;
                                    long rangeEnd = 0;
                                    long rangeLength = 0;
                                    String[] rangeParts = rageHeader.split("=");
                                    if (rangeParts.length < 2) {
                                        return ServerResponse.badRequest()
                                                .bodyValue("Invalid Range header format");
                                    }

                                    String[] ranges = rangeParts[1].split("-");
                                    if (ranges.length < 1) {
                                        return ServerResponse.badRequest()
                                                .bodyValue("Invalid Range header format");
                                    }

                                    rangeStart = Long.parseLong(ranges[0]);
                                    try {
                                        rangeEnd = ranges.length > 1 && !ranges[1].isEmpty()
                                                ? Long.parseLong(ranges[1])
                                                : resource.contentLength() - 1; // Если end не указан, используем конец файла
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                    rangeLength = rangeEnd - rangeStart + 1;
                                    try {
                                        return ServerResponse.status(HttpStatus.PARTIAL_CONTENT)
                                                .contentType(MediaTypeFactory.getMediaType(resource).orElse(MediaType.APPLICATION_OCTET_STREAM))
                                                .header(HttpHeaders.CONTENT_RANGE, "bytes " + rangeStart + "-" + rangeEnd + "/" + resource.contentLength())
                                                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(rangeLength))
                                                .body(Mono.just(resource), Resource.class);
                                    } catch (IOException e) {
                                        throw new RuntimeException(e);
                                    }
                                })
                        .orElseGet(() -> ServerResponse.ok()
                                            .contentType(MediaTypeFactory.getMediaType(resource).orElse(MediaType.APPLICATION_OCTET_STREAM))
                                            .body(Mono.just(resource), Resource.class));
                /*ServerResponse.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM) // Или другой подходящий тип
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filmId + ".mp4" + "\"")
                .body(Mono.just(resource), Resource.class);*/
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
}
