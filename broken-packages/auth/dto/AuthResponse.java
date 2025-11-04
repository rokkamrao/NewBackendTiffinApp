package com.tiffin.api.auth.dto;

import com.tiffin.api.user.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private boolean success;
    private String message;
    private String token;
    private String phone;
    private String name;
    private String email;
    private Role role;
    private List<String> dietary;
    private List<String> allergies;
    private String referralCode;
    private Boolean emailNotifications;
    private Boolean isNewUser;
    
    // Manual builder method for compatibility
    public static AuthResponseBuilder builder() {
        return new AuthResponseBuilder();
    }
    
    public static class AuthResponseBuilder {
        private boolean success;
        private String message;
        private String token;
        private String phone;
        private String name;
        private String email;
        private Role role;
        private List<String> dietary;
        private List<String> allergies;
        private String referralCode;
        private Boolean emailNotifications;
        private Boolean isNewUser;
        
        public AuthResponseBuilder success(boolean success) {
            this.success = success;
            return this;
        }
        
        public AuthResponseBuilder message(String message) {
            this.message = message;
            return this;
        }
        
        public AuthResponseBuilder token(String token) {
            this.token = token;
            return this;
        }
        
        public AuthResponseBuilder phone(String phone) {
            this.phone = phone;
            return this;
        }
        
        public AuthResponseBuilder name(String name) {
            this.name = name;
            return this;
        }
        
        public AuthResponseBuilder email(String email) {
            this.email = email;
            return this;
        }
        
        public AuthResponseBuilder role(Role role) {
            this.role = role;
            return this;
        }
        
        public AuthResponseBuilder dietary(List<String> dietary) {
            this.dietary = dietary;
            return this;
        }
        
        public AuthResponseBuilder allergies(List<String> allergies) {
            this.allergies = allergies;
            return this;
        }
        
        public AuthResponseBuilder referralCode(String referralCode) {
            this.referralCode = referralCode;
            return this;
        }
        
        public AuthResponseBuilder emailNotifications(Boolean emailNotifications) {
            this.emailNotifications = emailNotifications;
            return this;
        }
        
        public AuthResponseBuilder isNewUser(Boolean isNewUser) {
            this.isNewUser = isNewUser;
            return this;
        }
        
        public AuthResponse build() {
            AuthResponse response = new AuthResponse();
            response.success = this.success;
            response.message = this.message;
            response.token = this.token;
            response.phone = this.phone;
            response.name = this.name;
            response.email = this.email;
            response.role = this.role;
            response.dietary = this.dietary;
            response.allergies = this.allergies;
            response.referralCode = this.referralCode;
            response.emailNotifications = this.emailNotifications;
            response.isNewUser = this.isNewUser;
            return response;
        }
    }
}