package com.example.backend.mappers;

import com.example.backend.dto.MoviesInList;
import com.example.backend.models.Movies;

public class MoviesMapper {
    public static MoviesInList toDto(Movies movie) {
        return new MoviesInList(movie.getId(), movie.getTitle());
    }
}