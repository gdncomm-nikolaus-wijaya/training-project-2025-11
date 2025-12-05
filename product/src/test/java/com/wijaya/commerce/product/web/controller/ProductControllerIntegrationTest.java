package com.wijaya.commerce.product.web.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import com.mongodb.assertions.Assertions;
import com.wijaya.commerce.product.BaseIntegrationTest;
import com.wijaya.commerce.product.constant.ProductApiPath;
import com.wijaya.commerce.product.modelDb.CategoryDbModel;
import com.wijaya.commerce.product.modelDb.ProductDbModel;

public class ProductControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    void getDetailProduct_Success() throws Exception {
        // Seed categories first - create them manually to ensure proper ID mapping
        CategoryDbModel category1 = CategoryDbModel.builder()
                .id("cat-001")
                .name("Smartphones")
                .description("Mobile phones and smartphones")
                .build();

        CategoryDbModel category2 = CategoryDbModel.builder()
                .id("cat-002")
                .name("Electronics")
                .description("Electronic devices and gadgets")
                .build();

        mongoTemplate.save(category1);
        mongoTemplate.save(category2);

        // Verify categories were saved
        long categoryCount = mongoTemplate.count(new org.springframework.data.mongodb.core.query.Query(),
                CategoryDbModel.class);
        System.out.println("Categories in DB: " + categoryCount);

        // Then seed product
        ProductDbModel product = loadJson("data/product-success.json", ProductDbModel.class);
        System.out.println("Product categoryIds: " + product.getCategoryIds());
        mongoTemplate.save(product);

        // First call - should hit DB and cache the result
        mockMvc.perform(get(ProductApiPath.PRODUCT_DETAIL, product.getSku())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.sku").value(product.getSku()))
                .andExpect(jsonPath("$.data.name").value(product.getName()))
                .andExpect(jsonPath("$.data.description").value(product.getDescription()))
                .andExpect(jsonPath("$.data.brand").value(product.getBrand()))
                .andExpect(jsonPath("$.data.price").value(product.getPrice()))
                .andExpect(jsonPath("$.data.comparePrice").value(product.getComparePrice()))
                .andExpect(jsonPath("$.data.active").value(product.getActive()))
                .andExpect(jsonPath("$.data.discountPercentage").value(product.getDiscountPercentage()))
                // Images array assertions
                .andExpect(jsonPath("$.data.images").isArray())
                .andExpect(jsonPath("$.data.images[0].url").value(product.getImages().get(0).getUrl()))
                .andExpect(jsonPath("$.data.images[0].alt").value(product.getImages().get(0).getAlt()))
                .andExpect(jsonPath("$.data.images[0].primary").value(product.getImages().get(0).getIsPrimary()))
                .andExpect(jsonPath("$.data.images[1].url").value(product.getImages().get(1).getUrl()))
                .andExpect(jsonPath("$.data.images[1].alt").value(product.getImages().get(1).getAlt()))
                .andExpect(jsonPath("$.data.images[1].primary").value(product.getImages().get(1).getIsPrimary()))
                // Specifications object assertions
                .andExpect(jsonPath("$.data.specifications.Color").value(product.getSpecifications().get("Color")))
                .andExpect(jsonPath("$.data.specifications.Storage").value(product.getSpecifications().get("Storage")))
                // CategoryIds array assertions
                .andExpect(jsonPath("$.data.categories").isArray())
                .andExpect(jsonPath("$.data.categories.length()").value(2))
                .andExpect(jsonPath("$.data.categories[0].id").value("cat-001"))
                .andExpect(jsonPath("$.data.categories[0].name").value("Smartphones"))
                .andExpect(jsonPath("$.data.categories[1].id").value("cat-002"))
                .andExpect(jsonPath("$.data.categories[1].name").value("Electronics"));

        // Verify cache was created
        Object cachedValue = redisTemplate.opsForValue().get(product.getSku());
        Assertions.assertNotNull(cachedValue);

    }

    @Test
    void getDetailProduct_NotFound() throws Exception {
        // Call API with non-existent SKU
        mockMvc.perform(get(ProductApiPath.PRODUCT_DETAIL, "NON-EXISTENT-SKU")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isIAmATeapot())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").value("Product with sku NON-EXISTENT-SKU not found"));

        // Verify cache was not created
        Object cachedValue = redisTemplate.opsForValue().get("NON-EXISTENT-SKU");
        Assertions.assertNull(cachedValue);
    }

    @Test
    void getListProduct_WithoutFilters() throws Exception {
        // Seed categories
        CategoryDbModel category1 = CategoryDbModel.builder()
                .id("cat-001")
                .name("Smartphones")
                .build();
        mongoTemplate.save(category1);

        // Seed multiple products
        ProductDbModel product1 = ProductDbModel.builder()
                .sku("PRODUCT-001")
                .name("Product 1")
                .brand("Brand A")
                .price(10000L)
                .categoryIds(java.util.Arrays.asList("cat-001"))
                .build();

        ProductDbModel product2 = ProductDbModel.builder()
                .sku("PRODUCT-002")
                .name("Product 2")
                .brand("Brand B")
                .price(20000L)
                .categoryIds(java.util.Arrays.asList("cat-001"))
                .build();

        mongoTemplate.save(product1);
        mongoTemplate.save(product2);

        // Call API without filters
        mockMvc.perform(get(ProductApiPath.PRODUCT)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.pageSize").value(10));

        // Verify cache was created
        String cacheKey = "product_list_null_null_null_null_null_0_10";
        Object cachedValue = redisTemplate.opsForValue().get(cacheKey);
        Assertions.assertNotNull(cachedValue);
    }

    @Test
    void getListProduct_WithSearchFilter() throws Exception {
        // Seed categories
        CategoryDbModel category1 = CategoryDbModel.builder()
                .id("cat-001")
                .name("Electronics")
                .build();
        mongoTemplate.save(category1);

        // Seed products
        ProductDbModel product1 = ProductDbModel.builder()
                .sku("SAMSUNG-S23")
                .name("Samsung Galaxy S23")
                .brand("Samsung")
                .price(15000000L)
                .categoryIds(java.util.Arrays.asList("cat-001"))
                .build();

        ProductDbModel product2 = ProductDbModel.builder()
                .sku("IPHONE-14")
                .name("iPhone 14 Pro")
                .brand("Apple")
                .price(18000000L)
                .categoryIds(java.util.Arrays.asList("cat-001"))
                .build();

        mongoTemplate.save(product1);
        mongoTemplate.save(product2);

        // Search for "Samsung"
        mockMvc.perform(get(ProductApiPath.PRODUCT)
                .param("search", "Samsung")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].sku").value("SAMSUNG-S23"))
                .andExpect(jsonPath("$.data[0].name").value("Samsung Galaxy S23"));

        // Verify cache was created
        String cacheKey = "product_list_Samsung_null_null_null_null_0_10";
        Object cachedValue = redisTemplate.opsForValue().get(cacheKey);
        Assertions.assertNotNull(cachedValue);
    }

    @Test
    void getListProduct_WithCategoryFilter() throws Exception {
        // Seed categories
        CategoryDbModel category1 = CategoryDbModel.builder()
                .id("cat-001")
                .name("Smartphones")
                .build();
        mongoTemplate.save(category1);

        CategoryDbModel category2 = CategoryDbModel.builder()
                .id("cat-002")
                .name("Laptops")
                .build();
        mongoTemplate.save(category2);

        // Seed multiple products
        ProductDbModel product1 = ProductDbModel.builder()
                .sku("PRODUCT-001")
                .name("Product 1")
                .brand("Brand A")
                .price(10000L)
                .categoryIds(java.util.Arrays.asList("cat-001"))
                .build();

        ProductDbModel product2 = ProductDbModel.builder()
                .sku("PRODUCT-002")
                .name("Product 2")
                .brand("Brand B")
                .price(20000L)
                .categoryIds(java.util.Arrays.asList("cat-002"))
                .build();

        mongoTemplate.save(product1);
        mongoTemplate.save(product2);

        // Call API with category filter
        mockMvc.perform(get(ProductApiPath.PRODUCT)
                .param("category", "cat-002")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.pageSize").value(10));

        // Verify cache was created
        String cacheKey = "product_list_null_cat-002_null_null_null_0_10";
        Object cachedValue = redisTemplate.opsForValue().get(cacheKey);
        Assertions.assertNotNull(cachedValue);
    }

    @Test
    void getListProduct_WithPriceFilter() throws Exception {
        // Seed categories
        CategoryDbModel category1 = CategoryDbModel.builder()
                .id("cat-001")
                .name("Electronics")
                .build();
        mongoTemplate.save(category1);

        // Seed products with different prices
        ProductDbModel product1 = ProductDbModel.builder()
                .sku("CHEAP-PRODUCT")
                .name("Budget Phone")
                .brand("BrandX")
                .price(5000000L)
                .categoryIds(java.util.Arrays.asList("cat-001"))
                .build();

        ProductDbModel product2 = ProductDbModel.builder()
                .sku("EXPENSIVE-PRODUCT")
                .name("Flagship Phone")
                .brand("BrandY")
                .price(20000000L)
                .categoryIds(java.util.Arrays.asList("cat-001"))
                .build();

        mongoTemplate.save(product1);
        mongoTemplate.save(product2);

        // Filter by price range (10M - 25M)
        mockMvc.perform(get(ProductApiPath.PRODUCT)
                .param("minPrice", "10000000")
                .param("maxPrice", "25000000")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].sku").value("EXPENSIVE-PRODUCT"));
        // Verify cache was created
        String cacheKey = "product_list_null_null_10000000_25000000_null_0_10";
        Object cachedValue = redisTemplate.opsForValue().get(cacheKey);
        Assertions.assertNotNull(cachedValue);
    }

    @Test
    void getListProduct_WithPagination() throws Exception {
        // Seed categories
        CategoryDbModel category1 = CategoryDbModel.builder()
                .id("cat-001")
                .name("Electronics")
                .build();
        mongoTemplate.save(category1);

        // Seed 5 products
        for (int i = 1; i <= 5; i++) {
            ProductDbModel product = ProductDbModel.builder()
                    .sku("PRODUCT-" + i)
                    .name("Product " + i)
                    .brand("Brand")
                    .price(10000L * i)
                    .categoryIds(java.util.Arrays.asList("cat-001"))
                    .build();
            mongoTemplate.save(product);
        }

        // Get page 0 with size 2
        mockMvc.perform(get(ProductApiPath.PRODUCT)
                .param("page", "0")
                .param("size", "2")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.totalPages").value(3))
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.pageSize").value(2));

        String cacheKey = "product_list_null_null_null_null_null_0_2";
        Object cachedValue = redisTemplate.opsForValue().get(cacheKey);
        Assertions.assertNotNull(cachedValue);
    }
}
