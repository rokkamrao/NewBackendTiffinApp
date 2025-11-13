package com.tiffin.auth.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

/**
 * OTP Request DTO
 * 
 * Used for requesting OTP to be sent to a phone number
 */
@Data
public class OtpRequest {
    
    @NotBlank(message = "Phone number is required")
    // @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid phone number format")
    // @Size(min = 10, max = 10, message = "Phone number must be 10 digits")
    private String phone;
    
    /**
     * Constructor for OtpRequest
     */
    public OtpRequest() {}
    
    /**
     * Constructor with phone number
     * @param phone The phone number
     */
    public OtpRequest(String phone) {
        this.phone = phone;
    }
}