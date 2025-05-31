package com.example.backend.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;


import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("rooms")
public class Rooms {

    @Id
    @Column("id") // Можно не указывать, если имя совпадает
    private UUID id;

    @Column("owner_id")
    private UUID ownerId;

    @Column("movie_id")
    private UUID movieId;

    @Column("is_active")
    private boolean isActive = true;

    @CreatedDate
    @Column("created_at")
    private Instant createdAt = Instant.now();

    @Column("is_public")
    private boolean isPublic;

    @Column("last_activity_at")
    private Instant lastActivityAt = Instant.now();

    public void markUsed() {
        this.lastActivityAt = Instant.now();
        this.isActive = true;
    }

    public void markInactive() {
        this.isActive = false;
    }
}
