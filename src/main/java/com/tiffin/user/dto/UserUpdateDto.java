package com.tiffin.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO for user profile updates
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateDto {
    
    @Email(message = "Please provide a valid email address")
    private String email;
    
    @Size(max = 50, message = "First name cannot exceed 50 characters")
    private String firstName;
    
    @Size(max = 50, message = "Last name cannot exceed 50 characters")
    private String lastName;
    
    @Pattern(regexp = "^[+]?[1-9]\\d{1,14}$", message = "Please provide a valid phone number")
    private String phoneNumber;
    
    @Size(max = 255, message = "Profile image URL cannot exceed 255 characters")
    private String profileImageUrl;
    
    @Size(max = 10, message = "Preferred language code cannot exceed 10 characters")
    private String preferredLanguage;
}