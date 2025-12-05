package com.wijaya.commerce.cart.outbond.outbondServiceImpl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.wijaya.commerce.cart.constant.ProductApiPath;
import com.wijaya.commerce.cart.outbond.outbondModel.response.GetDetailProductOutbondResponse;
import com.wijaya.commerce.cart.outbond.outbondModel.response.WebResponse;
import com.wijaya.commerce.cart.outbond.outbondService.ProductOutbondService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class ProductOutbondServiceImpl implements ProductOutbondService {

    private final WebClient webClient;

    public ProductOutbondServiceImpl(@Value("${product.service.url}") String productServiceUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(productServiceUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Override
    public GetDetailProductOutbondResponse getProductDetail(String sku) {
        log.info("Fetching product detail for SKU: {}", sku);
        try {
            WebResponse<GetDetailProductOutbondResponse> wrapper = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(ProductApiPath.GET_DETAIL_PRODUCT+sku)
                            .build())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<WebResponse<GetDetailProductOutbondResponse>>() {
                    })
                    .block();

            if (wrapper == null || wrapper.getData() == null) {
                log.warn("Received null or empty response for SKU: {}", sku);
                return null;
            }

            GetDetailProductOutbondResponse response = wrapper.getData();
            log.info("Successfully fetched product detail for SKU: {}", sku);
            return response;
        } catch (Exception e) {
            log.error("Failed to get product detail for SKU: {}. Error: {}", sku, e.getMessage(), e);
            throw new RuntimeException("Failed to get product detail for SKU: " + sku, e);
        }
    }

}
