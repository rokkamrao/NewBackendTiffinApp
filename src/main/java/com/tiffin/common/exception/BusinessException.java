package com.tiffin.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Base exception for business logic errors
 * 
 * Provides standardized error codes and HTTP status codes for business rule violations
 * and domain-specific errors that should be handled gracefully.
 */
@Getter
public class BusinessException extends RuntimeException {
    
    private final String errorCode;
    private final HttpStatus httpStatus;
    
    public BusinessException(String errorCode, String message) {
        this(errorCode, message, HttpStatus.BAD_REQUEST);
    }
    
    public BusinessException(String errorCode, String message, HttpStatus httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }
    
    public BusinessException(String errorCode, String message, Throwable cause) {
        this(errorCode, message, HttpStatus.BAD_REQUEST, cause);
    }
    
    public BusinessException(String errorCode, String message, HttpStatus httpStatus, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }
    
    // Common business error codes
    public static class ErrorCodes {
        public static final String RESOURCE_NOT_FOUND = "RESOURCE_NOT_FOUND";
        public static final String DUPLICATE_RESOURCE = "DUPLICATE_RESOURCE";
        public static final String INVALID_STATE = "INVALID_STATE";
        public static final String INSUFFICIENT_PERMISSIONS = "INSUFFICIENT_PERMISSIONS";
        public static final String QUOTA_EXCEEDED = "QUOTA_EXCEEDED";
        public static final String BUSINESS_RULE_VIOLATION = "BUSINESS_RULE_VIOLATION";
        public static final String EXTERNAL_SERVICE_ERROR = "EXTERNAL_SERVICE_ERROR";
        public static final String DATA_INTEGRITY_ERROR = "DATA_INTEGRITY_ERROR";
    }
    
    // Factory methods for common exceptions
    public static BusinessException notFound(String resource) {
        return new BusinessException(ErrorCodes.RESOURCE_NOT_FOUND, 
            String.format("%s not found", resource), HttpStatus.NOT_FOUND);
    }
    
    public static BusinessException duplicate(String resource) {
        return new BusinessException(ErrorCodes.DUPLICATE_RESOURCE, 
            String.format("%s already exists", resource), HttpStatus.CONFLICT);
    }
    
    public static BusinessException invalidState(String message) {
        return new BusinessException(ErrorCodes.INVALID_STATE, message);
    }
    
    public static BusinessException insufficientPermissions() {
        return new BusinessException(ErrorCodes.INSUFFICIENT_PERMISSIONS, 
            "Insufficient permissions for this operation", HttpStatus.FORBIDDEN);
    }
    
    public static BusinessException quotaExceeded(String resource) {
        return new BusinessException(ErrorCodes.QUOTA_EXCEEDED, 
            String.format("Quota exceeded for %s", resource));
    }
    
    public static BusinessException businessRuleViolation(String rule) {
        return new BusinessException(ErrorCodes.BUSINESS_RULE_VIOLATION, 
            String.format("Business rule violation: %s", rule));
    }
    
    public static BusinessException externalServiceError(String service) {
        return new BusinessException(ErrorCodes.EXTERNAL_SERVICE_ERROR, 
            String.format("External service error: %s", service), HttpStatus.SERVICE_UNAVAILABLE);
    }
}