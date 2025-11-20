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
    private String token;
    private String refreshToken;
    private UserInfo user;
    private String otpId;
    
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
        private String profilePictureUrl;
        private boolean phoneVerified;
        private boolean emailVerified;
    }
    
    public static AuthResponse success(String message, String token, UserInfo user) {
        return AuthResponse.builder()
                .success(true)
                .message(message)
                .token(token)
                .user(user)
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
}