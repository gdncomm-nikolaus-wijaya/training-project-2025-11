package com.wijaya.commerce.cart.restWebModel.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeleteCartRequestWebModel {
    @NotBlank(message = "Cart ID is required")
    private String cartId;
}
