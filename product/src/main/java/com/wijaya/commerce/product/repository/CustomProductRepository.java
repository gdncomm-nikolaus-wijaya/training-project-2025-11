package com.wijaya.commerce.product.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.wijaya.commerce.product.modelDb.ProductDbModel;

public interface CustomProductRepository {
    Page<ProductDbModel> findProductsWithFilters(
            String search,
            String category,
            Long minPrice,
            Long maxPrice,
            String brand,
            Pageable pageable);
}
