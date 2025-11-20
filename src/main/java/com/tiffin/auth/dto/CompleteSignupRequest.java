package com.tiffin.auth.dto;

import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Data
public class CompleteSignupRequest {
    
    @NotBlank(message = "Phone number is required")
    private String phone;
    
    private String password;
    
    private String name;
    
    @Email(message = "Invalid email format")
    private String email;
    
    private String otp;
    
    public CompleteSignupRequest() {}
    
    public CompleteSignupRequest(String phone, String password, String name, String email, String otp) {
        this.phone = phone;
        this.password = password;
        this.name = name;
        this.email = email;
        this.otp = otp;
    }
}