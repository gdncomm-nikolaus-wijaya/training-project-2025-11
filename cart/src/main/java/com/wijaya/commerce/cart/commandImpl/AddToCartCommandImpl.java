package com.wijaya.commerce.cart.commandImpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.wijaya.commerce.cart.command.AddToCartCommand;
import com.wijaya.commerce.cart.commandImpl.model.AddToCartCommandRequest;
import com.wijaya.commerce.cart.commandImpl.model.AddToCartCommandResponse;
import com.wijaya.commerce.cart.modelDb.CartModelDb;
import com.wijaya.commerce.cart.modelDb.CartModelDb.CartItem;
import com.wijaya.commerce.cart.outbond.outbondModel.response.GetDetailProductOutbondResponse;
import com.wijaya.commerce.cart.outbond.outbondModel.response.GetDetailUserOutbondResponse;
import com.wijaya.commerce.cart.outbond.outbondService.ProductOutbondService;
import com.wijaya.commerce.cart.outbond.outbondService.UserOutbondService;
import com.wijaya.commerce.cart.repository.CartRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AddToCartCommandImpl implements AddToCartCommand {

    private final UserOutbondService userOutbondService;

    private final ProductOutbondService productOutbondService;

    private final CartRepository cartRepository;

    @Override
    public AddToCartCommandResponse doCommand(AddToCartCommandRequest request) {
        GetDetailUserOutbondResponse user = getUser(request.getUserId());
        GetDetailProductOutbondResponse product = getProduct(request.getProductSku());
        checkUserExistAndActive(user);
        checkProductExistAndActive(product);
        CartModelDb cartModelDb;
        if (request.getCartId() != null && cartRepository.existsById(request.getCartId())) {
            cartModelDb = cartRepository.findById(request.getCartId()).get();
            List<CartItem> items = cartModelDb.getItems();
            if (items == null) {
                items = new ArrayList<>();
            }
            cartModelDb.setItems(constructCartItem(items, product, request));
            cartModelDb.setUpdatedAt(LocalDateTime.now());
        } else {
            cartModelDb = constructCartModelDb(new ArrayList<>(), request, product);
        }
        cartRepository.save(cartModelDb);

        return addToCartToResponse(cartModelDb);
    }

    private AddToCartCommandResponse addToCartToResponse(CartModelDb cartModelDb) {
        return AddToCartCommandResponse.builder()
                .cartId(cartModelDb.getId().toHexString())
                .items(cartModelDb.getItems().stream().map(item -> {
                    return AddToCartCommandResponse.CartItemResponse.builder()
                            .productSku(item.getProductSku())
                            .productName(item.getProductName())
                            .price(item.getPrice())
                            .comparePrice(item.getComparePrice())
                            .discountPercentage(item.getDiscountPercentage())
                            .quantity(item.getQuantity())
                            .subtotal(item.getSubTotal().longValue())
                            .addedAt(item.getAddedAt())
                            .build();
                }).collect(Collectors.toList()))
                .summary(AddToCartCommandResponse.CartSummary.builder()
                        .totalItems(cartModelDb.getItems().size())
                        .totalPrice(cartModelDb.getItems().stream().mapToLong(item -> item.getSubTotal()).sum())
                        .totalDiscount(cartModelDb.getItems().stream()
                                .mapToLong(item -> item.getSubTotal() - item.getPrice())
                                .sum())
                        .build())
                .build();
    }

    private CartModelDb constructCartModelDb(List<CartItem> cartItems, AddToCartCommandRequest request,
            GetDetailProductOutbondResponse product) {
        CartModelDb cartModelDb = CartModelDb.builder()
                .userId(request.getUserId())
                .items(constructCartItem(cartItems, product, request))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return cartModelDb;
    }

    private List<CartItem> constructCartItem(List<CartItem> cartItems, GetDetailProductOutbondResponse product,
            AddToCartCommandRequest request) {
        cartItems.add(CartItem.builder()
                .productSku(product.getSku())
                .productName(product.getName())
                .price(product.getPrice())
                .comparePrice(product.getComparePrice())
                .discountPercentage(product.getDiscountPercentage())
                .quantity(request.getQuantity())
                .subTotal((int) (product.getPrice() * request.getQuantity()))
                .addedAt(LocalDateTime.now())
                .build());
        return cartItems;
    }

    private void checkUserExistAndActive(GetDetailUserOutbondResponse user) {
        if (user == null) {
            throw new RuntimeException("User not found");
        }
        if (!user.getStatus().equals("ACTIVE")) {
            throw new RuntimeException("User not active");
        }
    }

    private void checkProductExistAndActive(GetDetailProductOutbondResponse product) {
        if (product == null) {
            throw new RuntimeException("Product not found");
        }
        if (!product.getActive()) {
            throw new RuntimeException("Product not active");
        }
    }

    private GetDetailUserOutbondResponse getUser(String userId) {
        GetDetailUserOutbondResponse user = userOutbondService.getUserDetail(userId);
        return user;
    }

    private GetDetailProductOutbondResponse getProduct(String sku) {
        GetDetailProductOutbondResponse product = productOutbondService.getProductDetail(sku);
        return product;
    }

}
