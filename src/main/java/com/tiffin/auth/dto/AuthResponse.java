package com.tiffin.auth.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonInclude;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {
    
    private boolean success;
    private String message;
    private AuthData data;
    private String otpId; // For backward compatibility
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AuthData {
        private String token;
        private String refreshToken;
        private UserInfo user;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class UserInfo {
        private Long id;
        private String firstName;
        private String lastName;
        private String email;
        private String phone;
        private String name; // Computed field
        private String profilePictureUrl;
        private boolean phoneVerified;
        private boolean emailVerified;
        private String role;
        
        // Computed name field
        public String getName() {
            if (firstName != null && lastName != null) {
                return firstName + " " + lastName;
            } else if (firstName != null) {
                return firstName;
            } else if (lastName != null) {
                return lastName;
            }
            return email != null ? email : phone;
        }
    }
    
    public static AuthResponse success(String message, String token, UserInfo user) {
        AuthData authData = AuthData.builder()
                .token(token)
                .user(user)
                .build();
                
        return AuthResponse.builder()
                .success(true)
                .message(message)
                .data(authData)
                .build();
    }
    
    public static AuthResponse successWithOtp(String message, String otpId) {
        return AuthResponse.builder()
                .success(true)
                .message(message)
                .otpId(otpId)
                .build();
    }
    
    public static AuthResponse error(String message) {
        return AuthResponse.builder()
                .success(false)
                .message(message)
                .build();
    }
    
    // Legacy getters for backward compatibility
    public String getToken() {
        return data != null ? data.getToken() : null;
    }
    
    public UserInfo getUser() {
        return data != null ? data.getUser() : null;
    }
}