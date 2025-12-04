package com.wijaya.commerce.cart.commandImpl;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import com.wijaya.commerce.cart.command.DeleteCartCommand;
import com.wijaya.commerce.cart.commandImpl.model.DeleteCartCommandRequest;
import com.wijaya.commerce.cart.commandImpl.model.DeleteCartCommandResponse;
import com.wijaya.commerce.cart.exception.CartNotFoundException;
import com.wijaya.commerce.cart.repository.CartRepository;

import lombok.RequiredArgsConstructor;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeleteCartCommandImpl implements DeleteCartCommand {

    private final CartRepository cartRepository;

    @Override
    public DeleteCartCommandResponse doCommand(DeleteCartCommandRequest request) {
        cartRepository.findByIdAndUserId(request.getCartId(), request.getUserId())
                .ifPresentOrElse(cart -> {
                    try {
                        cartRepository.delete(cart);
                    } catch (Exception e) {
                        log.error("Failed to delete cart with cartId: {} for userId: {}, error: {}",
                                request.getCartId(), request.getUserId(), e.getMessage(), e);
                        throw new RuntimeException("Failed to delete cart from database", e);
                    }
                }, () -> {
                    log.error("Cart not found with cartId: {} for userId: {}",
                            request.getCartId(), request.getUserId());
                    throw new CartNotFoundException("Cart not found");
                });
        return DeleteCartCommandResponse.builder()
                .cartId(request.getCartId()).message("Cart deleted").build();
    }

}
