package com.wijaya.commerce.product.web.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.wijaya.commerce.product.exception.ProductNotFoundException;
import com.wijaya.commerce.product.restWebModel.response.WebResponse;

@RestControllerAdvice
@ControllerAdvice
public class ErrorController {

    @ExceptionHandler(ProductNotFoundException.class)
    @ResponseStatus(HttpStatus.I_AM_A_TEAPOT)
    public WebResponse<String> handleProductNotFound(ProductNotFoundException e) {
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

    // Catch-all for any other exceptions
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public WebResponse<String> handleGenericException(Exception e) {
        return WebResponse.<String>builder()
                .success(false)
                .data("An unexpected error occurred: " + e.getMessage())
                .build();
    }

    // Handle missing request parameters
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public WebResponse<String> handleMissingRequestParameter(MissingServletRequestParameterException e) {
        String paramName = e.getParameterName();
        return WebResponse.<String>builder()
                .success(false)
                .data(paramName + " is required")
                .build();
    }

}
