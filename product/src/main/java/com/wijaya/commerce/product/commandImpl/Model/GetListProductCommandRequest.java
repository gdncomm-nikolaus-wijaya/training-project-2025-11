package com.wijaya.commerce.product.commandImpl.Model;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder.Default;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetListProductCommandRequest {
    private String search;
    private String category;
    private Long minPrice;
    private Long maxPrice;
    private String brand;

    @Builder.Default
    @Min(value = 0, message = "Page min 0")
    private Integer page = 0;

    @Builder.Default
    @Min(value = 1, message = "size min 1")
    private Integer size = 10;
}
