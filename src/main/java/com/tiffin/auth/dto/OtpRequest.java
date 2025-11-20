package com.tiffin.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for sending OTP to phone number
 * 
 * This DTO contains phone number validation to ensure proper format
 * for international phone numbers with country code.
 */
@Data
@Schema(description = "Request to send OTP to a phone number")
public class OtpRequest {
    
    @Schema(
        description = "Phone number with country code (e.g., +1234567890)",
        example = "+1234567890",
        required = true,
        pattern = "^\\+[1-9]\\d{1,14}$"
    )
    @NotBlank(message = "Phone number is required")
    @Pattern(
        regexp = "^\\+[1-9]\\d{1,14}$",
        message = "Phone number must start with + followed by country code and digits (e.g., +1234567890)"
    )
    @Size(min = 10, max = 16, message = "Phone number must be between 10-16 characters including country code")
    private String phone;
    
    public OtpRequest() {}
    
    public OtpRequest(String phone) {
        this.phone = phone;
    }
}