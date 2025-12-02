package com.wijaya.commerce.product.restWebModel.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WebListResponseWithPage<T> {
    private boolean success;
    private List<T> data;
    private int totalPages;
    private long totalElements;
    private int currentPage;
    private int pageSize;

}
