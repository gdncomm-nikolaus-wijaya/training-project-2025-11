package com.wijaya.commerce.cart.web.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.wijaya.commerce.cart.command.AddToCartCommand;
import com.wijaya.commerce.cart.command.CommandExecutor;
import com.wijaya.commerce.cart.command.DeleteCartCommand;
import com.wijaya.commerce.cart.commandImpl.model.AddToCartCommandRequest;
import com.wijaya.commerce.cart.commandImpl.model.AddToCartCommandResponse;
import com.wijaya.commerce.cart.commandImpl.model.DeleteCartCommandRequest;
import com.wijaya.commerce.cart.commandImpl.model.DeleteCartCommandResponse;
import com.wijaya.commerce.cart.constant.CartApiPath;
import com.wijaya.commerce.cart.outbound.outboundModel.response.WebResponse;
import com.wijaya.commerce.cart.restWebModel.request.AddToCartRequestWebModel;
import com.wijaya.commerce.cart.restWebModel.request.DeleteCartRequestWebModel;
import com.wijaya.commerce.cart.restWebModel.response.AddToCartResponseWebModel;
import com.wijaya.commerce.cart.restWebModel.response.DeleteCartResponseWebModel;
import com.wijaya.commerce.cart.serviceImpl.helper.CartResponseHelper;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class CartController {
        private final CommandExecutor executor;

        @PostMapping(CartApiPath.ADD_TO_CART)
        public WebResponse<AddToCartResponseWebModel> addToCart(
                        @Valid @RequestBody AddToCartRequestWebModel request,
                        @RequestHeader(value = "X-User-Id", required = true) String userId) {
                AddToCartCommandRequest commandRequest = AddToCartCommandRequest.builder()
                                .userId(userId)
                                .cartId(request.getCartId())
                                .productSku(request.getProductSku())
                                .quantity(request.getQuantity())
                                .build();
                AddToCartCommandResponse response = executor.execute(AddToCartCommand.class, commandRequest);
                return WebResponse.<AddToCartResponseWebModel>builder().success(true)
                                .data(CartResponseHelper.toAddToCartResponseWebModel(response)).build();
        }

        @PostMapping(CartApiPath.CLEAR_ALL_CART)
        public WebResponse<DeleteCartResponseWebModel> clearAllCart(
                        @Valid @RequestBody DeleteCartRequestWebModel request,
                        @RequestHeader(value = "X-User-Id", required = true) String userId) {
                DeleteCartCommandRequest commandRequest = DeleteCartCommandRequest.builder()
                                .cartId(request.getCartId())
                                .userId(userId).build();

                DeleteCartCommandResponse commandResponse = executor.execute(DeleteCartCommand.class, commandRequest);

                DeleteCartResponseWebModel response = DeleteCartResponseWebModel.builder()
                                .cartId(commandResponse.getCartId())
                                .message("Cart deleted").build();
                return WebResponse.<DeleteCartResponseWebModel>builder().success(true)
                                .data(response).build();
        }
}
