package com.example.backend.models;

import com.example.backend.utilites.ObjectIdDeserializer;
import com.example.backend.utilites.ObjectIdSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.nio.file.Path;
import java.nio.file.Paths;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Scope(scopeName = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class Movies {
    @JsonSerialize(using = ObjectIdSerializer.class)
    @JsonDeserialize(using = ObjectIdDeserializer.class)
    private ObjectId id;
    private String title;
    private String poster;
    //private String file;
    //private double rating;
    //private String description;
    //private int releaseYear;
}