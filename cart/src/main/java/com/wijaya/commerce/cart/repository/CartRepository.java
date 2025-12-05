package com.wijaya.commerce.cart.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.wijaya.commerce.cart.modelDb.CartModelDb;

public interface CartRepository extends MongoRepository<CartModelDb, String> {
    Optional<CartModelDb> findByUserId(String userId);
    Optional<CartModelDb> findByIdAndUserId(String cartId, String userId);
    boolean existsByIdAndUserId(String cartId, String userId);

}
