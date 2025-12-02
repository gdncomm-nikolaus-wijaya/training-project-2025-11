package com.wijaya.commerce.product.restWebModel.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetListProductWebModel {
    private String sku;
    private String name;
    private String images;
    private Long price;
    private Long comparePrice;
    private Integer discountPercentage;
    private List<CategoryInfo> categories;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CategoryInfo {
        private String id;
        private String name;
    }
}
