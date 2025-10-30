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
}