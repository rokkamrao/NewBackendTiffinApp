package com.tiffin.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.*;

/**
 * Request DTO for user sign-in with credentials
 */
@Data
@Schema(description = "Request to sign in with phone/email and password")
public class SignInRequest {
    
    @Schema(
        description = "Phone number with country code or email address",
        example = "+1234567890"
    )
    @NotBlank(message = "Phone number or email is required")
    private String phone;
    
    @Schema(
        description = "User password",
        example = "SecurePass123!"
    )
    @NotBlank(message = "Password is required")
    private String password;

    public SignInRequest() {}

    public SignInRequest(String phone, String password) {
        this.phone = phone;
        this.password = password;
    }
}