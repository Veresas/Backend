package com.example.backend.configs;

import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;

import org.springframework.context.annotation.Primary;
import org.springframework.r2dbc.connection.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

@Configuration
@EnableR2dbcRepositories(basePackages = "com.example.backend.DAO")
public class PostgreConfig extends AbstractR2dbcConfiguration {



    @Value("${DB_HOST:localhost}")
    private String host;

    @Value("${DB_PORT:5432}")
    private int port;

    @Value("${DB_NAME:BackDB}")
    private String database;

    @Value("${DB_USER:postgres}")
    private String username;

    @Value("${DB_PASSWORD:password}")
    private String password;

    @Override
    public ConnectionFactory connectionFactory() {
        String connectionUrl = String.format(
                "r2dbc:postgresql://%s:%d/%s?user=%s&password=%s",
                host,
                port,
                database,
                username,
                password
        );

        return ConnectionFactories.get(connectionUrl);
    }

    /*@Override
    @Primary
    @Bean
    public ConnectionFactory connectionFactory() {
        return new PostgresqlConnectionFactory(
                PostgresqlConnectionConfiguration.builder()
                        .host(host)
                        .port(port)
                        .database(database)
                        .username(username)
                        .password(password)
                        .build()
        );
    }*/
}