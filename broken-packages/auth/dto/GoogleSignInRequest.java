package com.tiffin.api.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoogleSignInRequest {
    private String token;
    
    // Manual getters for compatibility
    public String getToken() {
        return token;
    }
    
    public void setToken(String token) {
        this.token = token;
    }
}