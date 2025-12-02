package com.wijaya.commerce.product.repositoryImpl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.wijaya.commerce.product.modelDb.ProductDbModel;
import com.wijaya.commerce.product.repository.CustomProductRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CustomProductRepositoryImpl implements CustomProductRepository {

    private final MongoTemplate mongoTemplate;

    @Override
    public Page<ProductDbModel> findProductsWithFilters(
            String search,
            String category,
            Long minPrice,
            Long maxPrice,
            String brand,
            Pageable pageable) {

        Query query = new Query();
        List<Criteria> criteriaList = new ArrayList<>();

        // Search filter - search in name field only (case-insensitive)
        // Using regex with "i" flag acts as a "contains" search
        if (search != null && !search.trim().isEmpty()) {
            criteriaList.add(Criteria.where("name").regex(search, "i"));
        }

        if (brand != null && !brand.trim().isEmpty()) {
            criteriaList.add(Criteria.where("brand").regex(brand, "i"));
        }

        // Category filter - match products that contain the specified categoryId
        if (category != null && !category.trim().isEmpty()) {
            criteriaList.add(Criteria.where("categoryIds").is(category));
        }

        // price filter
        if (minPrice != null) {
            criteriaList.add(Criteria.where("price").gte(minPrice));
        }

        if (maxPrice != null) {
            criteriaList.add(Criteria.where("price").lte(maxPrice));
        }

        // Combine all criteria with AND
        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }

        // get total data that match with query -> the function is for pagination, since
        // pagination need 3 params, result of quer, pageable, and total data
        long total = mongoTemplate.count(query, ProductDbModel.class);

        // Apply pagination
        query.with(pageable);

        // Execute query
        List<ProductDbModel> products = mongoTemplate.find(query, ProductDbModel.class);

        return new PageImpl<>(products, pageable, total);
    }
}
