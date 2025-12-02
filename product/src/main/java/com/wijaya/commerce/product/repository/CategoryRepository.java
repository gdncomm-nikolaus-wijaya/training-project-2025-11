package com.wijaya.commerce.product.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.wijaya.commerce.product.modelDb.CategoryDbModel;

public interface CategoryRepository extends MongoRepository<CategoryDbModel, String> {

}
