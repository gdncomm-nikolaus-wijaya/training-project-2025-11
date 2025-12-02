package com.wijaya.commerce.cart.commandImpl.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeleteCartCommandResponse {
    private String cartId;
    private String message;
}
