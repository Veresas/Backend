package com.example.backend.models;

import com.example.backend.utilites.ObjectIdDeserializer;
import com.example.backend.utilites.ObjectIdSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;


import java.time.Instant;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "rooms")
public class Rooms {

    @Id
    @JsonSerialize(using = ObjectIdSerializer.class)
    @JsonDeserialize(using = ObjectIdDeserializer.class)
    private ObjectId id;

    private String createdBy;

    private Set<String> userIds = new ConcurrentSkipListSet<>();

    private String movieId;

    @CreatedDate
    @Indexed(expireAfterSeconds = 43200) // 12 часов
    private Instant createdAt;
    private boolean isPublic;

}
