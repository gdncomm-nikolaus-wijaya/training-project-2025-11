package com.wijaya.commerce.cart.restWebModel.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeleteCartResponseWebModel {
    private String cartId;
    private String message;
}
