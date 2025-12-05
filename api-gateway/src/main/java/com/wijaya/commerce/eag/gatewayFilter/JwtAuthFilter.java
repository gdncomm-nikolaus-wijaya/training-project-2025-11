package com.wijaya.commerce.eag.gatewayFilter;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wijaya.commerce.eag.restWebModel.response.WebResponse;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.security.Key;

import org.springframework.beans.factory.annotation.Value;

@Slf4j
@Component
public class JwtAuthFilter extends AbstractGatewayFilterFactory<JwtAuthFilter.Config> {

    @Value("${jwt.secret}")
    private String jwtSecret;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public JwtAuthFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            // Check for Authorization header
            String authHeader = request.getHeaders().getFirst("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("Missing or invalid Authorization header");
                return writeErrorResponse(exchange, "Missing or invalid Authorization header");
            }

            String token = authHeader.substring(7);

            // Validate JWT and extract user claims
            try {
                Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
                var claims = Jwts.parserBuilder()
                        .setSigningKey(key)
                        .build()
                        .parseClaimsJws(token)
                        .getBody();

                // Extract user information from token claims
                String userId = claims.getSubject(); // or claims.get("userId", String.class) depending on your token

                log.debug("JWT token validated successfully for user: {}", userId);

                // Add user information to request headers for downstream services
                ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                        .header("X-User-Id", userId)
                        .build();

                // Continue filter chain with modified request
                return chain.filter(exchange.mutate().request(modifiedRequest).build());

            } catch (io.jsonwebtoken.ExpiredJwtException e) {
                log.warn("JWT token has expired: {}", e.getMessage());
                return writeErrorResponse(exchange, "JWT token has expired");
            } catch (io.jsonwebtoken.security.SignatureException e) {
                log.warn("JWT signature validation failed: {}", e.getMessage());
                return writeErrorResponse(exchange, "Invalid JWT signature");
            } catch (Exception e) {
                log.error("JWT validation failed: {}", e.getMessage());
                return writeErrorResponse(exchange, "JWT validation failed");
            }
        };
    }

    private Mono<Void> writeErrorResponse(ServerWebExchange exchange, String message) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        WebResponse<String> webResponse = WebResponse.<String>builder()
                .success(false)
                .data(message)
                .build();

        try {
            String jsonResponse = objectMapper.writeValueAsString(webResponse);
            DataBufferFactory bufferFactory = exchange.getResponse().bufferFactory();
            DataBuffer buffer = bufferFactory.wrap(jsonResponse.getBytes());
            return exchange.getResponse().writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            log.error("Error serializing response", e);
            return exchange.getResponse().setComplete();
        }
    }

    public static class Config {
    }

}
