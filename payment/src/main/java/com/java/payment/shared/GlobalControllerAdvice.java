package com.java.payment.shared;

import com.java.payment.api.model.ErrorResponse;
import com.java.payment.service.PaymentNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static org.springframework.http.HttpStatus.*;

@Slf4j
@ControllerAdvice
public class GlobalControllerAdvice {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .findFirst()
                .orElse("Invalid request");
        log.warn("Validation failed for: {}", message);
        return ResponseEntity
                .status(BAD_REQUEST)
                .body(buildErrorResponse(message));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex) {
        log.warn("Validation failed for: {}", ex.getMessage());
        return ResponseEntity
                .status(BAD_REQUEST)
                .body(buildErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException ex) {
        log.warn("Validation failed for: {}", ex.getMessage());
        return ResponseEntity
                .status(BAD_REQUEST)
                .body(buildErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> handleMissingHeader(MissingRequestHeaderException ex) {
        log.warn("Validation failed for: {}", ex.getMessage());
        return ResponseEntity
                .status(BAD_REQUEST)
                .body(buildErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePaymentNotFoundException(PaymentNotFoundException ex) {
        log.warn("Payment not found", ex);
        return ResponseEntity
                .status(NOT_FOUND)
                .body(buildErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity
                .status(INTERNAL_SERVER_ERROR)
                .body(buildErrorResponse("An unexpected error occurred"));
    }

    private ErrorResponse buildErrorResponse(String message) {
        return ErrorResponse.builder().message(message).build();
    }
}
