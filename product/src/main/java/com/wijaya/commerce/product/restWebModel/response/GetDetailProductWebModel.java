package com.wijaya.commerce.product.restWebModel.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetDetailProductWebModel {
    private String sku;
    private String name;
    private String description;
    private String brand;
    private Long price;
    private Long comparePrice;
    private Boolean active;
    private Integer discountPercentage;
    private List<ProductImage> images;
    private Map<String, String> specifications;
    private List<CategoryInfo> categories;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProductImage {
        private String url;
        private String alt;
        private boolean isPrimary;
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
