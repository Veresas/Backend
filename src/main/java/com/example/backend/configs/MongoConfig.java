package com.example.backend.configs;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

import javax.swing.*;

@Configuration
@EnableReactiveMongoRepositories(basePackages = "com.example.backend.dao")
public class MongoConfig extends AbstractReactiveMongoConfiguration {


    private final String dbName = "BackDB";
    private final String connectStr = "mongodb+srv://alexanderozhered:As23ercv@cluster0.ftpmx.mongodb.net/BackDB";

    @Override
    protected String getDatabaseName() {
        return dbName;
    }

    @Override
    public MongoClient reactiveMongoClient() {
        try {
            ConnectionString connectionString = new ConnectionString(connectStr);
            MongoClientSettings settings = MongoClientSettings.builder()
                    .applyConnectionString(connectionString)
                    .applyToSslSettings(builder -> builder.enabled(true))
                    .serverApi(ServerApi.builder().version(ServerApiVersion.V1).build())
                    .build();
            return MongoClients.create(settings);
        } catch (Exception e) {
            throw new RuntimeException("Failed to connect to MongoDB", e);
        }
    }

    @Bean
    public ReactiveMongoTemplate reactiveMongoTemplate() {
        return new ReactiveMongoTemplate(reactiveMongoClient(), getDatabaseName());
    }

}
