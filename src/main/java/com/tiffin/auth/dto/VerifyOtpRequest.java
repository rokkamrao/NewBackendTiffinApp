package com.tiffin.auth.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class VerifyOtpRequest {
    
    @NotBlank(message = "Phone number is required")
    private String phone;
    
    @NotBlank(message = "OTP is required")
    private String otp;
    
    public VerifyOtpRequest() {}
    
    public VerifyOtpRequest(String phone, String otp) {
        this.phone = phone;
        this.otp = otp;
    }
}