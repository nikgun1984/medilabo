package com.medilabo.notes;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@Slf4j
@Configuration
@EnableMongoAuditing
public class MongoConfig extends AbstractMongoClientConfiguration {

    @Value("${spring.data.mongodb.uri:mongodb://localhost:27017/notes_db}")
    private String mongoUri;

    @Override
    protected String getDatabaseName() {
        ConnectionString cs = new ConnectionString(mongoUri);
        String db = cs.getDatabase() != null ? cs.getDatabase() : "notes_db";
        log.info("MongoDB database name resolved to: {}", db);
        return db;
    }

    @Override
    public MongoClient mongoClient() {
        log.info("Connecting to MongoDB with URI host: {}",
                new ConnectionString(mongoUri).getHosts());
        ConnectionString connectionString = new ConnectionString(mongoUri);
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build();
        MongoClient client = MongoClients.create(settings);
        log.info("MongoClient created successfully");
        return client;
    }
}
