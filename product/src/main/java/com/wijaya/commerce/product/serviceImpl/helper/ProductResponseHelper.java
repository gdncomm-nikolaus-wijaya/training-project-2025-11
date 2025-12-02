package com.wijaya.commerce.product.serviceImpl.helper;

import java.util.stream.Collectors;
import java.util.List;

import org.springframework.stereotype.Service;

import com.wijaya.commerce.product.commandImpl.Model.GetDetailProductCommandResponse;
import com.wijaya.commerce.product.commandImpl.Model.GetListProductCommandResponse;
import com.wijaya.commerce.product.restWebModel.response.GetDetailProductWebModel;
import com.wijaya.commerce.product.restWebModel.response.GetListProductWebModel;

@Service
public class ProductResponseHelper {

  public static GetDetailProductWebModel toGetDetailProductWebResponse(
      GetDetailProductCommandResponse commandResponse) {
    GetDetailProductWebModel webResponse = CommonHelper.copyProperties(commandResponse,
        new GetDetailProductWebModel());
    List<GetDetailProductWebModel.ProductImage> images = commandResponse.getImages().stream()
        .map(image -> GetDetailProductWebModel.ProductImage.builder()
            .url(image.getUrl())
            .alt(image.getAlt())
            .isPrimary(image.getIsPrimary())
            .build())
        .collect(Collectors.toList());
    webResponse.setImages(images);
    List<GetDetailProductWebModel.CategoryInfo> categories = commandResponse.getCategories().stream()
        .map(category -> GetDetailProductWebModel.CategoryInfo.builder()
            .id(category.getId())
            .name(category.getName())
            .build())
        .collect(Collectors.toList());
    webResponse.setCategories(categories);

    return webResponse;
  }

  public static List<GetListProductWebModel> toGetListProductWebResponse(
      GetListProductCommandResponse commandResponse) {

    // Map products
    List<GetListProductWebModel> products = commandResponse.getProducts().stream()
        .map(product -> {
          List<GetListProductWebModel.CategoryInfo> categories = product.getCategories()
              .stream()
              .map(cat -> GetListProductWebModel.CategoryInfo.builder()
                  .id(cat.getId())
                  .name(cat.getName())
                  .build())
              .collect(Collectors.toList());

          return GetListProductWebModel.builder()
              .sku(product.getSku())
              .name(product.getName())
              .images(product.getImages())
              .price(product.getPrice())
              .comparePrice(product.getComparePrice())
              .discountPercentage(product.getDiscountPercentage())
              .categories(categories)
              .build();
        }).collect(Collectors.toList());

    return products;
  }

}
