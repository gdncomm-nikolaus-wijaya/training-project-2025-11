package com.wijaya.commerce.cart.commandImpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import com.wijaya.commerce.cart.command.AddToCartCommand;
import com.wijaya.commerce.cart.commandImpl.model.AddToCartCommandRequest;
import com.wijaya.commerce.cart.commandImpl.model.AddToCartCommandResponse;
import com.wijaya.commerce.cart.exception.ProductNotActiveException;
import com.wijaya.commerce.cart.exception.ProductNotFoundException;
import com.wijaya.commerce.cart.exception.UserNotActiveException;
import com.wijaya.commerce.cart.exception.UserNotFoundException;
import com.wijaya.commerce.cart.modelDb.CartModelDb;
import com.wijaya.commerce.cart.modelDb.CartModelDb.CartItem;
import com.wijaya.commerce.cart.outbond.outbondModel.response.GetDetailProductOutbondResponse;
import com.wijaya.commerce.cart.outbond.outbondModel.response.GetDetailUserOutbondResponse;
import com.wijaya.commerce.cart.outbond.outbondService.ProductOutbondService;
import com.wijaya.commerce.cart.outbond.outbondService.UserOutbondService;
import com.wijaya.commerce.cart.repository.CartRepository;

import lombok.RequiredArgsConstructor;

@Slf4j
@Service
@RequiredArgsConstructor
public class AddToCartCommandImpl implements AddToCartCommand {

    private final UserOutbondService userOutbondService;

    private final ProductOutbondService productOutbondService;

    private final CartRepository cartRepository;

    @Override
    public AddToCartCommandResponse doCommand(AddToCartCommandRequest request) {
        log.info("Starting add to cart command for userId: {}, productSku: {}, quantity: {}, cartId: {}",
                request.getUserId(), request.getProductSku(), request.getQuantity(), request.getCartId());

        try {
            // Get and validate user
            GetDetailUserOutbondResponse user = getUser(request.getUserId());
            checkUserExistAndActive(user);

            // Get and validate product
            GetDetailProductOutbondResponse product = getProduct(request.getProductSku());
            checkProductExistAndActive(product);

            CartModelDb cartModelDb;
            if (request.getCartId() != null && request.getUserId() != null
                    && cartRepository.existsByIdAndUserId(request.getCartId(), request.getUserId())) {
                cartModelDb = cartRepository.findByIdAndUserId(request.getCartId(), request.getUserId()).get();
                List<CartItem> items = cartModelDb.getItems();
                if (items == null) {
                    items = new ArrayList<>();
                }

                if (items.stream().anyMatch(item -> item.getProductSku().equals(request.getProductSku()))) {
                    // Update existing item
                    CartItem item = items.stream()
                            .filter(i -> i.getProductSku().equals(request.getProductSku()))
                            .findFirst()
                            .get();
                    item.setQuantity(request.getQuantity());
                    item.setSubTotal((int) (product.getPrice() * item.getQuantity()));
                    item.setAddedAt(LocalDateTime.now());
                } else {
                    // Add new item to the cart
                    items = constructCartItem(items, product, request);
                }
                cartModelDb.setItems(items);
                cartModelDb.setUpdatedAt(LocalDateTime.now());
            } else {
                cartModelDb = constructCartModelDb(new ArrayList<>(), request, product);
            }
            cartRepository.save(cartModelDb);

            AddToCartCommandResponse response = addToCartToResponse(cartModelDb);
            return response;

        } catch (RuntimeException e) {
            log.error("Error executing add to cart command for userId: {}, productSku: {}, error: {}",
                    request.getUserId(), request.getProductSku(), e.getMessage(), e);
            throw e;
        }
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
            log.error("User not found");
            throw new UserNotFoundException("User not found");
        }
        if (!user.getStatus().equals("ACTIVE")) {
            log.error("User is not active, status: {}", user.getStatus());
            throw new UserNotActiveException("User not active");
        }
    }

    private void checkProductExistAndActive(GetDetailProductOutbondResponse product) {
        if (product == null) {
            log.error("Product not found");
            throw new ProductNotFoundException("Product not found");
        }
        if (!product.getActive()) {
            log.error("Product is not active for sku: {}", product.getSku());
            throw new ProductNotActiveException("Product not active");
        }
    }

    private GetDetailUserOutbondResponse getUser(String userId) {
        try {
            GetDetailUserOutbondResponse user = userOutbondService.getUserDetail(userId);
            return user;
        } catch (Exception e) {
            log.error("Failed to fetch user details for userId: {}, error: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch user details", e);
        }
    }

    private GetDetailProductOutbondResponse getProduct(String sku) {
        try {
            GetDetailProductOutbondResponse product = productOutbondService.getProductDetail(sku);
            return product;
        } catch (Exception e) {
            log.error("Failed to fetch product details for sku: {}, error: {}", sku, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch product details", e);
        }
    }

}
