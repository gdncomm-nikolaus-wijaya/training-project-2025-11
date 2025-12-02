package com.wijaya.commerce.product.commandImpl.Model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GetListProductCommandResponse {
    private List<ProductInfo> products;
    private int totalPages;
    private long totalElements;
    private int currentPage;
    private int pageSize;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProductInfo {
        private String sku;
        private String name;
        private String images;
        private Long price;
        private Long comparePrice;
        private Integer discountPercentage;
        private List<CategoryInfo> categories;
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
