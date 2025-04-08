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

    @Indexed(unique = true)
    private String roomId;

    private String ownerId;
    private Set<String> users = new ConcurrentSkipListSet<>();

    private String movieId;
    private boolean isActive = true;
    @CreatedDate
    @Indexed(expireAfterSeconds = 43200) // 12 часов
    private Instant createdAt = Instant.now();;
    private boolean isPublic;
    private Instant lastActivityAt = Instant.now();

    public void markUsed() {
        this.lastActivityAt = Instant.now();
        this.isActive = true;
    }

    public void markInactive() {
        this.isActive = false;
    }

    public void addUser(String userId) {
        users.add(userId);
    }

    public void removeUser(String userId) {
        users.remove(userId);
    }

    public boolean isEmpty() {
        return users.isEmpty();
    }
}
