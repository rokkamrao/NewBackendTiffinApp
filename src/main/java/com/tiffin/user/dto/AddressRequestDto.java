package com.tiffin.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

/**
 * DTO for creating or updating addresses
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressRequestDto {
    
    @NotBlank(message = "Address label is required")
    @Size(max = 100, message = "Label cannot exceed 100 characters")
    private String label;
    
    @NotBlank(message = "Street address is required")
    @Size(max = 255, message = "Street address cannot exceed 255 characters")
    private String street;
    
    @Size(max = 100, message = "Apartment/Unit cannot exceed 100 characters")
    private String apartment;
    
    @NotBlank(message = "City is required")
    @Size(max = 100, message = "City cannot exceed 100 characters")
    private String city;
    
    @NotBlank(message = "State is required")
    @Size(max = 100, message = "State cannot exceed 100 characters")
    private String state;
    
    @NotBlank(message = "ZIP code is required")
    @Pattern(regexp = "^\\d{5,6}$", message = "ZIP code must be 5-6 digits")
    private String zipCode;
    
    @Size(max = 100, message = "Country cannot exceed 100 characters")
    @Builder.Default
    private String country = "India";
    
    @Size(max = 500, message = "Landmark cannot exceed 500 characters")
    private String landmark;
    
    @Size(max = 500, message = "Delivery instructions cannot exceed 500 characters")
    private String deliveryInstructions;
    
    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    private Double latitude;
    
    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    private Double longitude;
    
    private boolean isDefault;
}