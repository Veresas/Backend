package com.example.backend.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Scope(scopeName = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class Movies {
    @Id
    private ObjectId id;
    private String title;
    private String poster;
    //private double rating;
    //private String description;
    //private int releaseYear;
}