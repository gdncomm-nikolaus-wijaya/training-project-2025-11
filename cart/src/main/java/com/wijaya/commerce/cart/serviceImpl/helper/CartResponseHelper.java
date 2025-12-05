package com.wijaya.commerce.cart.serviceImpl.helper;

import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.wijaya.commerce.cart.commandImpl.model.AddToCartCommandResponse;
import com.wijaya.commerce.cart.restWebModel.response.AddToCartResponseWebModel;

@Service
public class CartResponseHelper {
    public static AddToCartResponseWebModel toAddToCartResponseWebModel(AddToCartCommandResponse response) {
        return AddToCartResponseWebModel.builder()
                .cartId(response.getCartId())
                .items(response.getItems().stream().map(item -> {
                    return AddToCartResponseWebModel.CartItemResponse.builder()
                            .productSku(item.getProductSku())
                            .productName(item.getProductName())
                            .productImage(item.getProductImage())
                            .price(item.getPrice())
                            .comparePrice(item.getComparePrice())
                            .discountPercentage(item.getDiscountPercentage())
                            .quantity(item.getQuantity())
                            .subtotal(item.getSubtotal())
                            .addedAt(item.getAddedAt())
                            .build();
                }).collect(Collectors.toList()))
                .summary(AddToCartResponseWebModel.CartSummary.builder()
                        .totalItems(response.getSummary().getTotalItems())
                        .totalPrice(response.getSummary().getTotalPrice())
                        .totalDiscount(response.getSummary().getTotalDiscount())
                        .build())
                .build();
    }
}
