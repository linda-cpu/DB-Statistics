package com.hszg.DB_Management.Config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexOperations;

import com.hszg.DB_Management.WeatherDataImport.WeatherEntity;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class MongoIndexConfig implements CommandLineRunner {

    private final MongoTemplate mongoTemplate;

    public MongoIndexConfig(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void run(String... args) {
        IndexOperations weatherIndexOps = mongoTemplate.indexOps(WeatherEntity.class);

        weatherIndexOps.ensureIndex(
            new Index()
                .on("eva", Direction.ASC)
                .on("timestamp", Direction.ASC)
                .named("eva_1_timestamp_1")
        );

    }
}