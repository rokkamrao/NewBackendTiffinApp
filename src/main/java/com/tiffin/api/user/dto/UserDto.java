package com.tiffin.api.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.Set;

@Data
public class UserDto {
    private Long id;
    private String phone;
    private String name;
    
    @Email
    private String email;
    
    private String avatarUrl;
    private Set<String> dietaryPreferences;
    private Set<AddressDto> addresses;
}

@Data
class UpdateUserRequest {
    @NotBlank
    private String name;
    
    @Email
    private String email;
    
    private Set<String> dietaryPreferences;
}

// AddressDto is defined in a separate file to avoid duplicate class definitions