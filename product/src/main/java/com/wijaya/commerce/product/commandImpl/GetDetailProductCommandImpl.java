package com.wijaya.commerce.product.commandImpl;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.wijaya.commerce.product.command.GetDetailProductCommand;
import com.wijaya.commerce.product.commandImpl.Model.GetDetailProductCommandRequest;
import com.wijaya.commerce.product.commandImpl.Model.GetDetailProductCommandResponse;
import com.wijaya.commerce.product.modelDb.CategoryDbModel;
import com.wijaya.commerce.product.modelDb.ProductDbModel;
import com.wijaya.commerce.product.repository.CategoryRepository;
import com.wijaya.commerce.product.repository.ProductRepository;
import com.wijaya.commerce.product.restWebModel.response.GetDetailProductWebModel;
import com.wijaya.commerce.product.service.helper.CacheService;
import com.wijaya.commerce.product.exception.ProductNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetDetailProductCommandImpl implements GetDetailProductCommand {

  private final ProductRepository productRepository;
  private final CategoryRepository categoryRepository;
  private final CacheService cacheService;

  @Override
  public GetDetailProductCommandResponse doCommand(GetDetailProductCommandRequest commandRequest) {
    GetDetailProductCommandResponse cache = null;
    try {
      cache = (GetDetailProductCommandResponse) cacheService.get(commandRequest.getSku());
      if (cache != null) {
        return cache;
      }
    } catch (Exception e) {
      log.warn("Failed to retrieve from cache for SKU: {}. Error: {}", commandRequest.getSku(), e.getMessage());
    }
    ProductDbModel product = productRepository.findBySku(commandRequest.getSku())
        .orElseThrow(() -> {
          log.error("Product not found with SKU: {}", commandRequest.getSku());
          return new ProductNotFoundException("Product with sku " + commandRequest.getSku() + " not found");
        });

    List<CategoryDbModel> categories = Collections.emptyList();
    if (isCategories(product)) {
      categories = categoryRepository.findAllById(product.getCategoryIds());
    }
    GetDetailProductCommandResponse response = mapToResponse(product, categories);

    try {
      cacheService.set(commandRequest.getSku(), response);
    } catch (Exception e) {
      log.warn("Failed to cache product with SKU: {}. Error: {}", commandRequest.getSku(), e.getMessage());
    }
    return response;
  }

  private boolean isCategories(ProductDbModel product) {
    return product.getCategoryIds() != null && !product.getCategoryIds().isEmpty();
  }

  private GetDetailProductCommandResponse mapToResponse(ProductDbModel product,
      List<CategoryDbModel> categories) {
    return GetDetailProductCommandResponse.builder()
        .sku(product.getSku())
        .name(product.getName())
        .description(product.getDescription())
        .brand(product.getBrand())
        .price(product.getPrice())
        .comparePrice(product.getComparePrice())
        .active(product.getActive())
        .discountPercentage(product.getDiscountPercentage())
        .images(mapImages(product.getImages()))
        .specifications(product.getSpecifications())
        .categories(mapCategories(categories))
        .createdAt(product.getCreatedAt())
        .updatedAt(product.getUpdatedAt())
        .build();
  }

  private List<GetDetailProductCommandResponse.ProductImage> mapImages(List<ProductDbModel.ProductImage> images) {
    if (images == null && images.size() == 0)
      return null;
    return images.stream()
        .map(img -> GetDetailProductCommandResponse.ProductImage.builder()
            .url(img.getUrl())
            .alt(img.getAlt())
            .isPrimary(img.getIsPrimary())
            .build())
        .collect(Collectors.toList());
  }

  private List<GetDetailProductCommandResponse.CategoryInfo> mapCategories(List<CategoryDbModel> categories) {
    if (categories == null)
      return null;
    return categories.stream()
        .map(cat -> GetDetailProductCommandResponse.CategoryInfo.builder()
            .id(cat.getId())
            .name(cat.getName())
            .build())
        .collect(Collectors.toList());
  }
}
