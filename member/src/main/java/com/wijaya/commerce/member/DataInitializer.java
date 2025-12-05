package com.wijaya.commerce.member;

import org.springframework.boot.CommandLineRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.wijaya.commerce.member.modelDb.MemberModelDb;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ObjectMapper objectMapper;
    private final MongoTemplate mongoTemplate;

    @Override
    public void run(String... args) throws Exception {
        objectMapper.registerModule(new JavaTimeModule());
        createIndex();
    }

    public void createIndex() {
        log.info("Creating MongoDB indexes...");

        try {
            // 1. Index for Category collection based on id (MongoDB automatically indexes
            // _id, but we'll ensure it)
            mongoTemplate.indexOps(MemberModelDb.class)
                    .ensureIndex(new Index()
                            .on("_id", Sort.Direction.ASC)
                            .named("idx_category_id"));
            log.info("Created index: idx_category_id on Category collection");

            // 2. Index for Member collection based on email (unique)
            mongoTemplate.indexOps(MemberModelDb.class)
                    .ensureIndex(new Index()
                            .on("email", Sort.Direction.ASC)
                            .unique()
                            .named("idx_email"));
            log.info("Created unique index: idx_email on member collection");

            // 3. Index for Member collection based on phoneNumber (unique)
            mongoTemplate.indexOps(MemberModelDb.class)
                    .ensureIndex(new Index()
                            .on("phoneNumber", Sort.Direction.ASC)
                            .unique()
                            .named("idx_phoneNumber"));
            log.info("Created unique index: idx_email on member collection");

            log.info("Successfully created all MongoDB indexes");

        } catch (Exception e) {
            log.error("Error while creating indexes: {}", e.getMessage(), e);
        }
    }

}
