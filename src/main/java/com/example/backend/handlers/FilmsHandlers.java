package com.example.backend.handlers;

import com.example.backend.models.Movies;
import com.example.backend.services.FilmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.core.io.Resource;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

@Component
public class FilmsHandlers {

    @Autowired
    private FilmsService filmsService;

    public Mono<ServerResponse> curentFilm (ServerRequest request){


        Flux<Movies> filmsList = filmsService.findAllFilms();

        return ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(filmsList, Movies.class);
    }

    public Mono<ServerResponse> filmList (ServerRequest request){
        return filmsService.findAllFilms()
                .collectList()
                .flatMap( films -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(films)
                )
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> getPoster (ServerRequest request){
        String filmId = request.pathVariable("id");
        Path posterPath = Paths.get("./sources/posters", filmId);
        Resource resource = new FileSystemResource(posterPath.toFile());
        return ServerResponse.ok()
                .contentType(MediaType.IMAGE_JPEG) // Или другой подходящий тип
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + posterPath + "\"")
                .body(Mono.just(resource), Resource.class);
    }

    public Mono<ServerResponse> addFile (ServerRequest request){
    return request.multipartData().flatMap(parts -> {
        var fileName = UUID.randomUUID().toString();
        var title = ((FormFieldPart) parts.getFirst("title"));
        var photoPart = parts.getFirst("photo"); // берём первый файл с ключом "photo"
        var videoPart = parts.getFirst("video"); // берём первый файл с ключом "video"

        // Обрабатываем текст
        Mono<Void> saveOnBd = title.content()
                .map(dataBuffer -> dataBuffer.toString(StandardCharsets.UTF_8))
                .reduce(String::concat)
                .flatMap(t -> filmsService.save(new Movies(null, t, fileName))
                        .then());

        // Сохраняем фото
        Mono<Void> savePhoto = saveFile(photoPart.content(), fileName + ".jpg", true);

        // Сохраняем видео
        Mono<Void> saveVideo = saveFile(videoPart.content(), fileName + ".mp4", false);


        return Mono.when(saveOnBd, savePhoto, saveVideo)
                .then(ServerResponse.ok().bodyValue("Files uploaded successfully"));
    });
    }

    private Mono<Void> saveFile(Flux<DataBuffer> dataBufferFlux, String fileName, Boolean isPhoto) {
        if(isPhoto){
            fileName = "./sources/posters/" + fileName;
        } else {
            fileName = "./sources/films/" + fileName;
        }
        Path filePath = Paths.get(fileName);
        return DataBufferUtils.write(dataBufferFlux, filePath, StandardOpenOption.CREATE, StandardOpenOption.WRITE)
                .doOnError(e -> System.err.println("Error saving file: " + e.getMessage())) // Логируем ошибки
                .then();
    }
}
