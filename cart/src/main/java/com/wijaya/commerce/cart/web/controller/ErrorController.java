package com.wijaya.commerce.cart.web.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.wijaya.commerce.cart.exception.CartNotFoundException;
import com.wijaya.commerce.cart.exception.ProductNotActiveException;
import com.wijaya.commerce.cart.exception.ProductNotFoundException;
import com.wijaya.commerce.cart.exception.UserNotActiveException;
import com.wijaya.commerce.cart.exception.UserNotFoundException;
import com.wijaya.commerce.cart.outbound.outboundModel.response.WebResponse;

@RestControllerAdvice
@ControllerAdvice
public class ErrorController {
    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.I_AM_A_TEAPOT)
    public WebResponse<String> handleUserNotFound(UserNotFoundException e) {
        return WebResponse.<String>builder()
                .success(false)
                .data(e.getMessage())
                .build();
    }

    @ExceptionHandler(ProductNotFoundException.class)
    @ResponseStatus(HttpStatus.I_AM_A_TEAPOT)
    public WebResponse<String> handleProductNotFound(ProductNotFoundException e) {
        return WebResponse.<String>builder()
                .success(false)
                .data(e.getMessage())
                .build();
    }

    @ExceptionHandler(UserNotActiveException.class)
    @ResponseStatus(HttpStatus.I_AM_A_TEAPOT)
    public WebResponse<String> handleUserNotActive(UserNotActiveException e) {
        return WebResponse.<String>builder()
                .success(false)
                .data(e.getMessage())
                .build();
    }

    @ExceptionHandler(ProductNotActiveException.class)
    @ResponseStatus(HttpStatus.I_AM_A_TEAPOT)
    public WebResponse<String> handleProductNotActive(ProductNotActiveException e) {
        return WebResponse.<String>builder()
                .success(false)
                .data(e.getMessage())
                .build();
    }

    @ExceptionHandler(CartNotFoundException.class)
    @ResponseStatus(HttpStatus.I_AM_A_TEAPOT)
    public WebResponse<String> handleCartNotFound(CartNotFoundException e) {
        return WebResponse.<String>builder()
                .success(false)
                .data(e.getMessage())
                .build();
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public WebResponse<String> defaultHandleRuntimeException(RuntimeException e) {
        return WebResponse.<String>builder()
                .success(false)
                .data(e.getMessage())
                .build();
    }

    // Handle @Valid validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public WebResponse<Map<String, String>> handleValidationErrors(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return WebResponse.<Map<String, String>>builder()
                .success(false)
                .data(errors)
                .build();
    }

    // Handle missing required request headers (e.g., X-User-Id)
    @ExceptionHandler(MissingRequestHeaderException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public WebResponse<String> handleMissingRequestHeader(MissingRequestHeaderException e) {
        String errorMessage = String.format("Required request header '%s' is missing", e.getHeaderName());
        return WebResponse.<String>builder()
                .success(false)
                .data(errorMessage)
                .build();
    }

    // Handle malformed JSON or request body issues
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public WebResponse<String> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        return WebResponse.<String>builder()
                .success(false)
                .data("Malformed JSON request: " + e.getMostSpecificCause().getMessage())
                .build();
    }

    // Catch-all for any other exceptions
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public WebResponse<String> handleGenericException(Exception e) {
        return WebResponse.<String>builder()
                .success(false)
                .data("An unexpected error occurred: " + e.getMessage())
                .build();
    }
}
