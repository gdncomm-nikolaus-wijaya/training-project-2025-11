package com.wijaya.commerce.product;

import java.io.InputStream;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
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

    @Override
    public void run(String... args) throws Exception {
        objectMapper.registerModule(new JavaTimeModule());
        initializeCategories();
        initializeProducts();
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

}
