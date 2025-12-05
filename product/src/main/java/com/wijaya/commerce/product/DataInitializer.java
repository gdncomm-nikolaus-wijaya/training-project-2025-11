package com.wijaya.commerce.product;

import java.io.InputStream;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.wijaya.commerce.product.modelDb.CategoryDbModel;
import com.wijaya.commerce.product.modelDb.ProductDbModel;
import com.wijaya.commerce.product.repository.CategoryRepository;
import com.wijaya.commerce.product.repository.ProductRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ObjectMapper objectMapper;
    private final MongoTemplate mongoTemplate;

    @Override
    public void run(String... args) throws Exception {
        objectMapper.registerModule(new JavaTimeModule());
        initializeCategories();
        initializeProducts();
        createIndex();
    }

    private void initializeCategories() throws Exception {
        long count = categoryRepository.count();
        if (count > 0) {
            log.info("Database already contains {} categories. Skipping category initialization.", count);
            return;
        }

        log.info("Initializing category data from JSON file...");

        try {
            ClassPathResource resource = new ClassPathResource("data/category.json");
            InputStream inputStream = resource.getInputStream();

            List<CategoryDbModel> categories = objectMapper.readValue(
                    inputStream,
                    new TypeReference<List<CategoryDbModel>>() {
                    });

            List<CategoryDbModel> savedCategories = categoryRepository.saveAll(categories);

            log.info("Successfully initialized {} categories to database.", savedCategories.size());

        } catch (Exception e) {
            log.error("Error while initializing category data: {}", e.getMessage(), e);
            throw e;
        }
    }

    private void initializeProducts() throws Exception {
        long count = productRepository.count();
        if (count > 0) {
            log.info("Database already contains {} products. Skipping product initialization.", count);
            return;
        }

        log.info("Initializing product data from JSON file...");

        try {
            ClassPathResource resource = new ClassPathResource("data/products.json");
            InputStream inputStream = resource.getInputStream();

            List<ProductDbModel> products = objectMapper.readValue(
                    inputStream,
                    new TypeReference<List<ProductDbModel>>() {
                    });

            List<ProductDbModel> savedProducts = productRepository.saveAll(products);

            log.info("Successfully initialized {} products to database.", savedProducts.size());

        } catch (Exception e) {
            log.error("Error while initializing product data: {}", e.getMessage(), e);
            throw e;
        }
    }

    public void createIndex() {
        log.info("Creating MongoDB indexes...");

        try {
            // 1. Index for Category collection based on id (MongoDB automatically indexes
            // _id, but we'll ensure it)
            mongoTemplate.indexOps(CategoryDbModel.class)
                    .ensureIndex(new Index()
                            .on("_id", Sort.Direction.ASC)
                            .named("idx_category_id"));
            log.info("Created index: idx_category_id on Category collection");

            // 2. Index for Product collection based on sku (unique)
            mongoTemplate.indexOps(ProductDbModel.class)
                    .ensureIndex(new Index()
                            .on("sku", Sort.Direction.ASC)
                            .unique()
                            .named("idx_product_sku"));
            log.info("Created unique index: idx_product_sku on Product collection");

            // 3. Compound index for Product search queries (name, categoryIds, price,
            // brand)
            // This supports queries filtering by name (text search), category, price range,
            // and brand
            mongoTemplate.indexOps(ProductDbModel.class)
                    .ensureIndex(new Index()
                            .on("name", Sort.Direction.ASC)
                            .on("categoryIds", Sort.Direction.ASC)
                            .on("price", Sort.Direction.ASC)
                            .on("brand", Sort.Direction.ASC)
                            .named("idx_product_search"));
            log.info("Created compound index: idx_product_search on Product collection");

            mongoTemplate.indexOps(ProductDbModel.class)
                    .ensureIndex(new Index()
                            .on("name", Sort.Direction.ASC)
                            .named("idx_product_name"));
            log.info("Created index: idx_product_name on Product collection");

            mongoTemplate.indexOps(ProductDbModel.class)
                    .ensureIndex(new Index()
                            .on("price", Sort.Direction.ASC)
                            .named("idx_product_price"));
            log.info("Created index: idx_product_price on Product collection");

            mongoTemplate.indexOps(ProductDbModel.class)
                    .ensureIndex(new Index()
                            .on("brand", Sort.Direction.ASC)
                            .named("idx_product_brand"));
            log.info("Created index: idx_product_brand on Product collection");

            mongoTemplate.indexOps(ProductDbModel.class)
                    .ensureIndex(new Index()
                            .on("categoryIds", Sort.Direction.ASC)
                            .named("idx_product_categoryIds"));
            log.info("Created index: idx_product_categoryIds on Product collection");

            log.info("Successfully created all MongoDB indexes");

        } catch (Exception e) {
            log.error("Error while creating indexes: {}", e.getMessage(), e);
        }
    }

}
