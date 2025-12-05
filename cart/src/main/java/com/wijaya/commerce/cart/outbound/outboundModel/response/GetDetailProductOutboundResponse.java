package com.wijaya.commerce.cart.outbound.outboundModel.response;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GetDetailProductOutboundResponse {
    private String sku;
    private String name;
    private String description;
    private String brand;
    private Long price;
    private Long comparePrice;
    private Integer discountPercentage;
    private List<ProductImage> images;
    private Map<String, String> specifications;
    private List<CategoryInfo> categories;
    private Boolean active;

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
