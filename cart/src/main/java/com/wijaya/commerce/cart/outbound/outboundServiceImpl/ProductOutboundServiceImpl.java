package com.wijaya.commerce.cart.outbound.outboundServiceImpl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.wijaya.commerce.cart.constant.ProductApiPath;
import com.wijaya.commerce.cart.outbound.outboundModel.response.GetDetailProductOutboundResponse;
import com.wijaya.commerce.cart.outbound.outboundModel.response.WebResponse;
import com.wijaya.commerce.cart.outbound.outboundService.ProductOutboundService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ProductOutboundServiceImpl implements ProductOutboundService {

    private final WebClient webClient;

    public ProductOutboundServiceImpl(@Value("${product.service.url}") String productServiceUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(productServiceUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Override
    public WebResponse<?> getProductDetail(String sku) {
        log.info("Fetching product detail for SKU: {}", sku);
        try {
            WebResponse<?> response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(ProductApiPath.GET_DETAIL_PRODUCT + sku)
                            .build())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<WebResponse<?>>() {
                    })
                    .block();
            log.info("Successfully fetched product detail for SKU: {}", sku);
            return response;
        } catch (Exception e) {
            log.error("Failed to get product detail for SKU: {}. Error: {}", sku, e.getMessage(), e);
            return WebResponse.builder()
                    .success(false)
                    .data("Failed to get product detail: " + e.getMessage())
                    .build();
        }
    }

}
