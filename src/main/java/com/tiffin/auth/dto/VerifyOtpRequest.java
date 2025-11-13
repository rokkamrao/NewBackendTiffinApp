package com.tiffin.auth.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

/**
 * Verify OTP Request DTO
 * 
 * Used for verifying OTP sent to a phone number
 */
@Data
public class VerifyOtpRequest {
    
    @NotBlank(message = "Phone number is required")
    // @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid phone number format")
    // @Size(min = 10, max = 10, message = "Phone number must be 10 digits")
    private String phone;
    
    @NotBlank(message = "OTP is required")
    // @Pattern(regexp = "^\\d{6}$", message = "OTP must be 6 digits")
    private String otp;
    
    /**
     * Constructor for VerifyOtpRequest
     */
    public VerifyOtpRequest() {}
    
    /**
     * Constructor with phone and OTP
     * @param phone The phone number
     * @param otp The OTP code
     */
    public VerifyOtpRequest(String phone, String otp) {
        this.phone = phone;
        this.otp = otp;
    }
}