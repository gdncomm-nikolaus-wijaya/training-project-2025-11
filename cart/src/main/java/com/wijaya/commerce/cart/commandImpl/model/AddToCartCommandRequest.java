package com.wijaya.commerce.cart.commandImpl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddToCartCommandRequest {
    private String userId;
    private String cartId;
    private String productSku;
    private int quantity;

}
