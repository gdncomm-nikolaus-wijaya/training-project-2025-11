package com.wijaya.commerce.product.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.wijaya.commerce.product.modelDb.ProductDbModel;

import java.util.Optional;

public interface ProductRepository extends MongoRepository<ProductDbModel, String>, CustomProductRepository {
    Optional<ProductDbModel> findBySku(String sku);
}
