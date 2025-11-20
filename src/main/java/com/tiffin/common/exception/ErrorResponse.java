package com.tiffin.common.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Standardized error response format for all API errors
 * 
 * Provides consistent error structure across the entire application
 * with detailed information for debugging and user feedback.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Standardized error response format")
public class ErrorResponse {
    
    @Schema(description = "Indicates if the operation was successful", example = "false")
    private boolean success;
    
    @Schema(description = "Specific error code for programmatic handling", example = "VALIDATION_FAILED")
    private String errorCode;
    
    @Schema(description = "Human-readable error message", example = "Validation failed")
    private String message;
    
    @Schema(description = "Timestamp when the error occurred", example = "2025-11-20T12:30:45.123Z")
    private LocalDateTime timestamp;
    
    @Schema(description = "API path where the error occurred", example = "/api/auth/login")
    private String path;
    
    @Schema(description = "Field-specific validation errors", example = "{\"email\": \"Invalid email format\", \"password\": \"Password too short\"}")
    private Map<String, String> fieldErrors;
    
    @Schema(description = "Additional error details for debugging", example = "{\"traceId\": \"abc123\", \"userId\": \"user456\"}")
    private Map<String, Object> additionalDetails;
}