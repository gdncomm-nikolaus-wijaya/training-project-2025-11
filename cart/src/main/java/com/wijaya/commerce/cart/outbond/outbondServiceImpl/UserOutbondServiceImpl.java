package com.wijaya.commerce.cart.outbond.outbondServiceImpl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.wijaya.commerce.cart.constant.MemberApiPath;
import com.wijaya.commerce.cart.outbond.outbondModel.response.GetDetailUserOutbondResponse;
import com.wijaya.commerce.cart.outbond.outbondModel.response.WebResponse;
import com.wijaya.commerce.cart.outbond.outbondService.UserOutbondService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UserOutbondServiceImpl implements UserOutbondService {

    private final WebClient webClient;

    public UserOutbondServiceImpl(@Value("${member.service.url}") String memberServiceUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(memberServiceUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Override
    public GetDetailUserOutbondResponse getUserDetail(String id) {
        log.info("Fetching user detail for userId: {}", id);
        try {
            WebResponse<GetDetailUserOutbondResponse> wrapper = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(MemberApiPath.GET_DETAIL_USER)
                            .queryParam("userId", id)
                            .build())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<WebResponse<GetDetailUserOutbondResponse>>() {
                    })
                    .block();

            if (wrapper == null || wrapper.getData() == null) {
                log.warn("Received null or empty response for userId: {}", id);
                return null;
            }

            GetDetailUserOutbondResponse response = wrapper.getData();
            log.info("Successfully fetched user detail for userId: {}", id);
            return response;
        } catch (Exception e) {
            log.error("Failed to get user detail for userId: {}. Error: {}", id, e.getMessage(), e);
            throw new RuntimeException("Failed to get user detail for id: " + id, e);
        }
    }

}
