package com.wijaya.commerce.product.web.controller;

import com.wijaya.commerce.product.commandImpl.Model.GetListProductCommandResponse;
import com.wijaya.commerce.product.restWebModel.response.WebListResponseWithPage;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wijaya.commerce.product.command.CommandExecutor;
import com.wijaya.commerce.product.command.GetDetailProductCommand;
import com.wijaya.commerce.product.command.GetListProductCommand;
import com.wijaya.commerce.product.commandImpl.Model.GetDetailProductCommandRequest;
import com.wijaya.commerce.product.commandImpl.Model.GetDetailProductCommandResponse;
import com.wijaya.commerce.product.commandImpl.Model.GetListProductCommandRequest;
import com.wijaya.commerce.product.constant.ProductApiPath;
import com.wijaya.commerce.product.restWebModel.response.GetDetailProductWebModel;
import com.wijaya.commerce.product.restWebModel.response.GetListProductWebModel;
import com.wijaya.commerce.product.restWebModel.response.WebResponse;
import com.wijaya.commerce.product.serviceImpl.helper.ProductResponseHelper;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ProductController {
    private final CommandExecutor commandExecutor;

    @GetMapping(ProductApiPath.PRODUCT_DETAIL)
    public WebResponse<GetDetailProductWebModel> getProduct(@PathVariable String sku) {
        GetDetailProductCommandRequest commandRequest = GetDetailProductCommandRequest.builder()
                .sku(sku)
                .build();

        GetDetailProductCommandResponse commandResponse = commandExecutor.execute(GetDetailProductCommand.class,
                commandRequest);

        return WebResponse.<GetDetailProductWebModel>builder()
                .success(true)
                .data(ProductResponseHelper.toGetDetailProductWebResponse(commandResponse))
                .build();
    }

    @GetMapping(ProductApiPath.PRODUCT)
    public WebListResponseWithPage<GetListProductWebModel> getListProduct(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Long minPrice,
            @RequestParam(required = false) Long maxPrice,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        GetListProductCommandRequest commandRequest = GetListProductCommandRequest.builder()
                .search(search)
                .category(category)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .brand(brand)
                .page(page)
                .size(size)
                .build();

        GetListProductCommandResponse commandResponse = commandExecutor.execute(GetListProductCommand.class,
                commandRequest);

        return WebListResponseWithPage.<GetListProductWebModel>builder()
                .success(true)
                .data(ProductResponseHelper.toGetListProductWebResponse(commandResponse))
                .totalPages(commandResponse.getTotalPages())
                .currentPage(commandResponse.getCurrentPage())
                .totalElements(commandResponse.getTotalElements())
                .pageSize(commandResponse.getPageSize())
                .build();
    }
}
