package com.tiffin.api.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import com.tiffin.api.user.dto.UserDto;

@Data
class LoginRequest {
    @NotBlank
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be 10 digits")
    private String phone;
}

@Data
class VerifyOtpRequest {
    @NotBlank
    private String phone;
    
    @NotBlank
    @Pattern(regexp = "^[0-9]{6}$", message = "OTP must be 6 digits")
    private String otp;
}

@Data
class LoginResponse {
    private String token;
    private UserDto user;
}