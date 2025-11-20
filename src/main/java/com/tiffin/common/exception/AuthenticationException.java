package com.tiffin.common.exception;

import lombok.Getter;

/**
 * Base exception for authentication-related errors
 * 
 * Provides standardized error codes and messages for authentication failures
 * including invalid credentials, expired tokens, and authorization issues.
 */
@Getter
public class AuthenticationException extends RuntimeException {
    
    private final String errorCode;
    
    public AuthenticationException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public AuthenticationException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    // Common authentication error codes
    public static class ErrorCodes {
        public static final String INVALID_CREDENTIALS = "INVALID_CREDENTIALS";
        public static final String TOKEN_EXPIRED = "TOKEN_EXPIRED";
        public static final String TOKEN_INVALID = "TOKEN_INVALID";
        public static final String OTP_EXPIRED = "OTP_EXPIRED";
        public static final String OTP_INVALID = "OTP_INVALID";
        public static final String USER_NOT_FOUND = "USER_NOT_FOUND";
        public static final String USER_INACTIVE = "USER_INACTIVE";
        public static final String ACCOUNT_LOCKED = "ACCOUNT_LOCKED";
        public static final String PASSWORD_RESET_REQUIRED = "PASSWORD_RESET_REQUIRED";
    }
    
    // Factory methods for common exceptions
    public static AuthenticationException invalidCredentials() {
        return new AuthenticationException(ErrorCodes.INVALID_CREDENTIALS, 
            "Invalid username or password");
    }
    
    public static AuthenticationException tokenExpired() {
        return new AuthenticationException(ErrorCodes.TOKEN_EXPIRED, 
            "Authentication token has expired");
    }
    
    public static AuthenticationException tokenInvalid() {
        return new AuthenticationException(ErrorCodes.TOKEN_INVALID, 
            "Authentication token is invalid");
    }
    
    public static AuthenticationException otpExpired() {
        return new AuthenticationException(ErrorCodes.OTP_EXPIRED, 
            "OTP has expired. Please request a new one");
    }
    
    public static AuthenticationException otpInvalid() {
        return new AuthenticationException(ErrorCodes.OTP_INVALID, 
            "Invalid OTP code");
    }
    
    public static AuthenticationException userNotFound() {
        return new AuthenticationException(ErrorCodes.USER_NOT_FOUND, 
            "User not found");
    }
    
    public static AuthenticationException userInactive() {
        return new AuthenticationException(ErrorCodes.USER_INACTIVE, 
            "User account is inactive");
    }
}