package com.wijaya.commerce.member.web.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.wijaya.commerce.member.exception.EmailAlreadyExistsException;
import com.wijaya.commerce.member.exception.FailedLoginException;
import com.wijaya.commerce.member.exception.InvalidTokenException;
import com.wijaya.commerce.member.exception.PhoneNumberAlreadyExistsException;
import com.wijaya.commerce.member.exception.UserNotFoundException;
import com.wijaya.commerce.member.restWebModel.response.WebResponse;

@RestControllerAdvice
@ControllerAdvice
public class ErrorController {
    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.I_AM_A_TEAPOT)
    public WebResponse<String> handleProductNotFound(UserNotFoundException e) {
        return WebResponse.<String>builder()
                .success(false)
                .data(e.getMessage())
                .build();
    }

    @ExceptionHandler(FailedLoginException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public WebResponse<String> handleProductNotFound(FailedLoginException e) {
        return WebResponse.<String>builder()
                .success(false)
                .data(e.getMessage())
                .build();
    }

    @ExceptionHandler(InvalidTokenException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public WebResponse<String> handleInvalidToken(InvalidTokenException e) {
        return WebResponse.<String>builder()
                .success(false)
                .data(e.getMessage())
                .build();
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public WebResponse<String> handleEmailAlreadyExistsException(EmailAlreadyExistsException e) {
        return WebResponse.<String>builder()
                .success(false)
                .data(e.getMessage())
                .build();
    }

    @ExceptionHandler(PhoneNumberAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public WebResponse<String> handlePhoneNumberAlreadyExistsException(PhoneNumberAlreadyExistsException e) {
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
