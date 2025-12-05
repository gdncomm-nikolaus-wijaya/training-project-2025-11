package com.wijaya.commerce.cart.commandImpl.model;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddToCartCommandResponse {
    private String cartId;

    private List<CartItemResponse> items;

    private CartSummary summary;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class CartItemResponse {
        private String productSku;
        private String productName;
        private String productImage;
        private Long price;
        private Long comparePrice;
        private Integer discountPercentage;
        private Integer quantity;
        private Long subtotal;
        private LocalDateTime addedAt;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class CartSummary {
        private Integer totalItems;
        private Long totalPrice;
        private Long totalDiscount;
    }

}
