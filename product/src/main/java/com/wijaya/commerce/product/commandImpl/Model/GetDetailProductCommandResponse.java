package com.wijaya.commerce.product.commandImpl.Model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class GetDetailProductCommandResponse {
    private String sku;
    private String name;
    private String description;
    private String brand;
    private long price;
    private long comparePrice;
    private int discountPercentage;
    private boolean active;
    private List<ProductImage> images;
    private Map<String, String> specifications;
    private List<CategoryInfo> categories;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProductImage {
        private String url;
        private String alt;
        private Boolean isPrimary;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CategoryInfo {
        private String id;
        private String name;
    }
}
