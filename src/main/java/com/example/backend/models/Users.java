package com.example.backend.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("users")
@Scope(scopeName = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class Users {

    @Id
    @Column("id")
    private UUID id; // Заменяем ObjectId на UUID

    @Column("username") // Пример явного указания имени столбца
    private String name;

    @Column("email")
    private String email;

    @Column("password")
    private String password;

    @Column("role_id")
    private UUID roleId;

    @Transient
    private List<Movies> movies = new ArrayList<>();
}