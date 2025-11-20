package com.tiffin.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.*;

/**
 * Request DTO for completing user signup with detailed information
 * 
 * This DTO contains comprehensive validation for user registration
 * including personal details, credentials, and contact information.
 */
@Data
@Schema(description = "Request to complete user signup with personal details")
public class CompleteSignupRequest {
    
    @Schema(
        description = "Phone number with country code (must match OTP verification)",
        example = "+1234567890"
    )
    @NotBlank(message = "Phone number is required")
    @Pattern(
        regexp = "^\\+[1-9]\\d{1,14}$",
        message = "Phone number must start with + followed by country code and digits"
    )
    private String phone;
    
    @Schema(
        description = "User password (minimum 8 characters, must contain uppercase, lowercase, number, and special character)",
        example = "SecurePass123!"
    )
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 128, message = "Password must be between 8-128 characters")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
        message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character"
    )
    private String password;
    
    @Schema(
        description = "Confirm password (must match password)",
        example = "SecurePass123!"
    )
    @NotBlank(message = "Password confirmation is required")
    private String confirmPassword;
    
    @Schema(
        description = "User's first name",
        example = "John"
    )
    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2-50 characters")
    @Pattern(regexp = "^[A-Za-z\\s'-]+$", message = "First name can only contain letters, spaces, hyphens, and apostrophes")
    private String firstName;
    
    @Schema(
        description = "User's last name",
        example = "Doe"
    )
    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2-50 characters")
    @Pattern(regexp = "^[A-Za-z\\s'-]+$", message = "Last name can only contain letters, spaces, hyphens, and apostrophes")
    private String lastName;
    
    @Schema(
        description = "User's email address",
        example = "john.doe@example.com"
    )
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;
    
    @Schema(
        description = "User's preferred language code",
        example = "en",
        allowableValues = {"en", "es", "fr", "de", "hi", "ta", "te"}
    )
    @Pattern(regexp = "^(en|es|fr|de|hi|ta|te)$", message = "Unsupported language code")
    private String preferredLanguage = "en";
    
    public CompleteSignupRequest() {}
    
    public CompleteSignupRequest(String phone, String password, String confirmPassword, 
                               String firstName, String lastName, String email, String preferredLanguage) {
        this.phone = phone;
        this.password = password;
        this.confirmPassword = confirmPassword;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.preferredLanguage = preferredLanguage;
    }
    
    /**
     * Custom validation to ensure password and confirmPassword match
     */
    @AssertTrue(message = "Password and confirm password must match")
    private boolean isPasswordConfirmed() {
        return password != null && password.equals(confirmPassword);
    }
}