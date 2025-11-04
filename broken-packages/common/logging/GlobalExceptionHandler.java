package com.tiffin.api.common.logging;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthenticationException(AuthenticationException ex) {
        String traceId = MDC.get("traceId");
        log.warn("🔒 Authentication failed, traceId={}: {}", traceId, ex.getMessage());
        
        String message = "Invalid credentials. Please check your phone/email and password.";
        if (ex instanceof BadCredentialsException) {
            message = "Invalid phone number, email, or password.";
        }
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                        "success", false,
                        "message", message,
                        "traceId", traceId != null ? traceId : "unknown"
                ));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatus(ResponseStatusException ex) {
        String traceId = MDC.get("traceId");
        log.warn("⚠️ Client error {}, traceId={}, reason={}", ex.getStatusCode(), traceId, ex.getReason());
        
        return ResponseEntity.status(ex.getStatusCode())
                .body(Map.of(
                        "success", false,
                        "message", ex.getReason() != null ? ex.getReason() : "An error occurred",
                        "traceId", traceId != null ? traceId : "unknown"
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception ex) {
        String traceId = MDC.get("traceId");
        log.error("❌ Unhandled exception, traceId={}: {}", traceId, ex.getMessage(), ex);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "success", false,
                        "message", "An unexpected error occurred. Please try again later.",
                        "details", ex.getMessage() != null ? ex.getMessage() : "No details available",
                        "traceId", traceId != null ? traceId : "unknown"
                ));
    }
}
