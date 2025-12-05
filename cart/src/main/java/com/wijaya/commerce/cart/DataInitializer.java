package com.wijaya.commerce.cart;    


import org.springframework.boot.CommandLineRunner;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wijaya.commerce.cart.modelDb.CartModelDb;
import com.wijaya.commerce.cart.repository.CartRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final MongoTemplate mongoTemplate;

    @Override
    public void run(String... args) throws Exception {
        createIndex();
    }

    public void createIndex() {
        log.info("Creating MongoDB indexes...");

        try {
            // 1. Index for cart collection based on id (MongoDB automatically indexes
            // _id, but we'll ensure it)
            mongoTemplate.indexOps(CartModelDb.class)
                    .ensureIndex(new Index()
                            .on("_id", Sort.Direction.ASC)
                            .on("cartId", Sort.Direction.ASC)
                            .on("userId", Sort.Direction.ASC)
                            .named("idx_cart_id_user_id"));
            log.info("Created index: idx_cart_id_user_id on Category collection");

            log.info("Successfully created all MongoDB indexes");

        } catch (Exception e) {
            log.error("Error while creating indexes: {}", e.getMessage(), e);
        }
    }

}
