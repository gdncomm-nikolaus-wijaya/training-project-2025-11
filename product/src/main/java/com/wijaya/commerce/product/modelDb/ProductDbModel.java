package com.wijaya.commerce.product.modelDb;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.wijaya.commerce.product.constant.CollectionProductName;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = CollectionProductName.PRODUCT)
public class ProductDbModel {

    @Id
    private String id;

    // Basic Info
    private String name;
    private String description;
    private String sku;
    private String brand;

    // Pricing
    private Long price;
    private Long comparePrice;
    private Integer discountPercentage;

    // Categories (reference to categories collection)
    private List<String> categoryIds;

    // Images
    private List<ProductImage> images;

    // Specifications
    private Map<String, String> specifications;

    // Status
    private Boolean active;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Nested class for Product Images
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ProductImage {
        private String url;
        private String alt;
        private Boolean isPrimary;
    }
}
