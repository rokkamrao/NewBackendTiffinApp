package com.tiffin.auth.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class OtpRequest {
    
    @NotBlank(message = "Phone number is required")
    private String phone;
    
    public OtpRequest() {}
    
    public OtpRequest(String phone) {
        this.phone = phone;
    }
}