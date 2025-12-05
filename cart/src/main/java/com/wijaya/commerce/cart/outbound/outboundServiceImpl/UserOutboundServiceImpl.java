package com.wijaya.commerce.cart.outbound.outboundServiceImpl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.wijaya.commerce.cart.constant.MemberApiPath;
import com.wijaya.commerce.cart.outbound.outboundModel.response.GetDetailUserOutboundResponse;
import com.wijaya.commerce.cart.outbound.outboundModel.response.WebResponse;
import com.wijaya.commerce.cart.outbound.outboundService.UserOutboundService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UserOutboundServiceImpl implements UserOutboundService {

    private final WebClient webClient;

    public UserOutboundServiceImpl(@Value("${member.service.url}") String memberServiceUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(memberServiceUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Override
    public WebResponse<?> getUserDetail(String id) {
        log.info("Fetching user detail for userId: {}", id);
        try {
            WebResponse<?> response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(MemberApiPath.GET_DETAIL_USER)
                            .queryParam("userId", id)
                            .build())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<WebResponse<?>>() {
                    })
                    .block();

            log.info("Successfully fetched user detail for userId: {}. Success: {}", id,
                    response != null ? response.isSuccess() : "null");
            return response;
        } catch (Exception e) {
            log.error("Failed to get user detail for userId: {}. Error: {}", id, e.getMessage(), e);
            return WebResponse.builder()
                    .success(false)
                    .data("Failed to get user detail: " + e.getMessage())
                    .build();
        }
    }

}
