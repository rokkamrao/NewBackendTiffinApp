package com.tiffin.auth.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

/**
 * Google Sign-In Request DTO
 * 
 * Used for Google OAuth authentication
 */
@Data
public class GoogleSignInRequest {
    
    @NotBlank(message = "Google token is required")
    private String token;
    
    /**
     * Constructor for GoogleSignInRequest
     */
    public GoogleSignInRequest() {}
    
    /**
     * Constructor with token
     * @param token The Google OAuth token
     */
    public GoogleSignInRequest(String token) {
        this.token = token;
    }
}