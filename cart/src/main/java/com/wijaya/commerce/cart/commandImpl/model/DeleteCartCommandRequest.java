package com.wijaya.commerce.cart.commandImpl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeleteCartCommandRequest {
    private String cartId;
    private String userId;
}
