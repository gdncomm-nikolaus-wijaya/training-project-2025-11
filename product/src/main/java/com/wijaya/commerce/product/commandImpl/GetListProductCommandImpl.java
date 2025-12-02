package com.wijaya.commerce.product.commandImpl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.wijaya.commerce.product.command.GetListProductCommand;
import com.wijaya.commerce.product.commandImpl.Model.GetListProductCommandRequest;
import com.wijaya.commerce.product.commandImpl.Model.GetListProductCommandResponse;
import com.wijaya.commerce.product.modelDb.CategoryDbModel;
import com.wijaya.commerce.product.modelDb.ProductDbModel;
import com.wijaya.commerce.product.repository.CategoryRepository;
import com.wijaya.commerce.product.repository.ProductRepository;
import com.wijaya.commerce.product.service.helper.CacheService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetListProductCommandImpl implements GetListProductCommand {
  private final ProductRepository productRepository;
  private final CategoryRepository categoryRepository;
  private final CacheService cacheService;

  @Override
  public GetListProductCommandResponse doCommand(GetListProductCommandRequest commandRequest) {
    // Create pageable with default values if not provided
    int page = commandRequest.getPage() != null ? commandRequest.getPage() : 0;
    int size = commandRequest.getSize() != null ? commandRequest.getSize() : 10;
    Pageable pageable = PageRequest.of(page, size);

    String key = "product_list_" + commandRequest.getSearch() + "_" + commandRequest.getCategory() + "_"
        + commandRequest.getMinPrice() + "_" + commandRequest.getMaxPrice() + "_"
        + commandRequest.getBrand()
        + "_" + page + "_" + size;

    GetListProductCommandResponse cache = (GetListProductCommandResponse) cacheService.get(key);
    if (cache != null) {
      return cache;
    }
    // Execute query with filters and pagination
    Page<ProductDbModel> productPage = productRepository.findProductsWithFilters(
        commandRequest.getSearch(),
        commandRequest.getCategory(),
        commandRequest.getMinPrice(),
        commandRequest.getMaxPrice(),
        commandRequest.getBrand(),
        pageable);

    // Extract all unique category IDs from products
    List<String> allCategoryIds = getListCategoryById(productPage);

    // get category detail based on id product
    Map<String, CategoryDbModel> categoryMap = categoryRepository.findAllById(allCategoryIds).stream()
        .collect(Collectors.toMap(category -> category.getId(), category -> category));

    List<GetListProductCommandResponse.ProductInfo> productCommandResponse = mapProductToResponse(
        productPage,
        categoryMap);

    // Build response with pagination metadata
    GetListProductCommandResponse response = GetListProductCommandResponse.builder()
        .products(productCommandResponse)
        .totalPages(productPage.getTotalPages())
        .totalElements(productPage.getTotalElements())
        .currentPage(productPage.getNumber())
        .pageSize(productPage.getSize())
        .build();

    cacheService.set(key, response);

    return response;
  }

  private List<String> getListCategoryById(Page<ProductDbModel> productPage) {
    List<String> categoryIds = productPage.getContent().stream()
        .flatMap(product -> product.getCategoryIds().stream())
        .distinct()
        .collect(Collectors.toList());
    return categoryIds;
  }

  private List<GetListProductCommandResponse.ProductInfo> mapProductToResponse(Page<ProductDbModel> productPage,
      Map<String, CategoryDbModel> categoryMap) {
    return productPage.getContent().stream()
        .map(product -> {
          // Get primary image URL
          String primaryImageUrl = product.getImages() != null
              && !product.getImages().isEmpty()
              ? product.getImages().stream()
              .filter(img -> img
                  .getIsPrimary() != null
                  && img.getIsPrimary())
              .findFirst()
              .map(ProductDbModel.ProductImage::getUrl)
              .orElse(product.getImages()
                  .get(0)
                  .getUrl())
              : null;

          // Map categories with resolved names
          List<GetListProductCommandResponse.CategoryInfo> categories = product
              .getCategoryIds().stream()
              .map(categoryId -> {
                CategoryDbModel category = categoryMap.get(categoryId);
                return GetListProductCommandResponse.CategoryInfo
                    .builder()
                    .id(categoryId)
                    .name(category.getName())
                    .build();
              })
              .collect(Collectors.toList());

          return GetListProductCommandResponse.ProductInfo.builder()
              .sku(product.getSku())
              .name(product.getName())
              .images(primaryImageUrl)
              .price(product.getPrice())
              .comparePrice(product.getComparePrice())
              .discountPercentage(product.getDiscountPercentage())
              .categories(categories)
              .build();
        })
        .collect(Collectors.toList());
  }

}
