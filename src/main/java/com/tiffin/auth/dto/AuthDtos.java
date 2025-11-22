package com.tiffin.auth.dto;

import lombok.Data;

public class AuthDtos {
    @Data
    public static class LoginRequest {
        private String email;
        private String password;
    }

    @Data
    public static class SignupRequest {
        private String name;
        private String email;
        private String phone;
        private String password;
    }

    @Data
    public static class OtpRequest {
        private String phone;
    }

    @Data
    public static class OtpVerifyRequest {
        private String phone;
        private String otp;
    }

    @Data
    public static class JwtResponse {
        private String token;
        private String type = "Bearer";
        private Long id;
        private String name;
        private String email;
        private String phone;
        private String role;
        private boolean isNewUser;

        public JwtResponse(String accessToken, Long id, String name, String email, String phone, String role,
                boolean isNewUser) {
            this.token = accessToken;
            this.id = id;
            this.name = name;
            this.email = email;
            this.phone = phone;
            this.role = role;
            this.isNewUser = isNewUser;
        }

        // Constructor for backward compatibility if needed
        public JwtResponse(String accessToken, Long id, String name, String email, String role) {
            this(accessToken, id, name, email, null, role, false);
        }
    }
}
