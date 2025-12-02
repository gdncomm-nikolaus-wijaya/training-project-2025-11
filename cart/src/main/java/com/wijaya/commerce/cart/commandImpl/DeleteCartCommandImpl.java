package com.wijaya.commerce.cart.commandImpl;

import org.springframework.stereotype.Service;

import com.wijaya.commerce.cart.command.DeleteCartCommand;
import com.wijaya.commerce.cart.commandImpl.model.DeleteCartCommandRequest;
import com.wijaya.commerce.cart.commandImpl.model.DeleteCartCommandResponse;
import com.wijaya.commerce.cart.repository.CartRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeleteCartCommandImpl implements DeleteCartCommand {

    private final CartRepository cartRepository;

    @Override
    public DeleteCartCommandResponse doCommand(DeleteCartCommandRequest request) {
        cartRepository.findById(request.getCartId()).ifPresent(cart -> {
            cartRepository.delete(cart);
        });
        return DeleteCartCommandResponse.builder()
                .cartId(request.getCartId()).message("Cart deleted").build();
    }

}
