package com.wijaya.commerce.product.web.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.wijaya.commerce.product.command.CommandExecutor;
import com.wijaya.commerce.product.command.GetDetailProductCommand;
import com.wijaya.commerce.product.commandImpl.Model.GetDetailProductCommandRequest;
import com.wijaya.commerce.product.commandImpl.Model.GetDetailProductCommandResponse;
import com.wijaya.commerce.product.constant.ProductApiPath;
import com.wijaya.commerce.product.restWebModel.response.GetDetailProductWebModel;
import com.wijaya.commerce.product.restWebModel.response.WebResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ProductController {
        private final CommandExecutor commandExecutor;

        @GetMapping(ProductApiPath.PRODUCT + "/{sku}")
        public WebResponse<GetDetailProductWebModel> getProduct(@PathVariable String sku) {
                GetDetailProductCommandRequest commandRequest = GetDetailProductCommandRequest.builder()
                                .sku(sku)
                                .build();

                GetDetailProductCommandResponse commandResponse = commandExecutor.execute(GetDetailProductCommand.class,
                                commandRequest);

                return WebResponse.<GetDetailProductWebModel>builder()
                                .success(true)
                                .data(mapToWebModel(commandResponse))
                                .build();
        }

        private GetDetailProductWebModel mapToWebModel(GetDetailProductCommandResponse response) {
                return GetDetailProductWebModel.builder()
                                .sku(response.getSku())
                                .name(response.getName())
                                .description(response.getDescription())
                                .brand(response.getBrand())
                                .price(response.getPrice())
                                .comparePrice(response.getComparePrice())
                                .discountPercentage(response.getDiscountPercentage())
                                .images(mapImages(response.getImages()))
                                .specifications(response.getSpecifications())
                                .categories(mapCategories(response.getCategories()))
                                .build();
        }

        private java.util.List<GetDetailProductWebModel.ProductImage> mapImages(
                        java.util.List<com.wijaya.commerce.product.commandImpl.Model.GetDetailProductCommandResponse.ProductImage> images) {
                if (images == null)
                        return null;
                return images.stream()
                                .map(img -> GetDetailProductWebModel.ProductImage.builder()
                                                .url(img.getUrl())
                                                .alt(img.getAlt())
                                                .isPrimary(img.getIsPrimary())
                                                .build())
                                .collect(java.util.stream.Collectors.toList());
        }

        private java.util.List<GetDetailProductWebModel.CategoryInfo> mapCategories(
                        java.util.List<com.wijaya.commerce.product.commandImpl.Model.GetDetailProductCommandResponse.CategoryInfo> categories) {
                if (categories == null)
                        return null;
                return categories.stream()
                                .map(cat -> GetDetailProductWebModel.CategoryInfo.builder()
                                                .id(cat.getId())
                                                .name(cat.getName())
                                                .build())
                                .collect(java.util.stream.Collectors.toList());
        }

}
