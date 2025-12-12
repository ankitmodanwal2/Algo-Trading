package com.myorg.trading.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.HashMap;
import java.util.Map;

/**
 * Centralized exception handling for REST controllers.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // --- CRITICAL FIX: Trap Dhan Errors & Prevent Logout Loop ---
    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<?> handleBrokerError(WebClientResponseException ex) {
        // 1. Log the REAL error from Dhan (e.g. "Token expired")
        System.err.println(">>> BROKER API ERROR: " + ex.getResponseBodyAsString());

        // 2. If Dhan says 401 (Unauthorized), send 400 (Bad Request) to Frontend.
        //    This prevents the Frontend from thinking *App Login* is expired.
        if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of(
                            "error", "broker_auth_failed",
                            "message", "Broker Connection Failed: Invalid or Expired Token. Please Re-link."
                    ));
        }

        // 3. For other errors, return 500
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "error", "broker_error",
                        "status", ex.getStatusCode().value(),
                        "message", "Broker rejected request. Check backend logs."
                ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "access_denied", "message", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneric(Exception ex) {
        ex.printStackTrace(); // Print stack trace to console for debugging
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "internal_error", "message", ex.getMessage()));
    }

    @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
    public ResponseEntity<?> handleAuthException(Exception ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "unauthorized", "message", ex.getMessage()));
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, Object> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(err -> {
            String field = err instanceof FieldError fe ? fe.getField() : err.getObjectName();
            String msg = err.getDefaultMessage();
            errors.put(field, msg);
        });
        return Map.of("error", "validation_failed", "fields", errors);
    }
}