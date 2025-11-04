package com.tiffin.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Address DTO for API responses
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressDto {
    
    private Long id;
    private String label;
    private String street;
    private String apartment;
    private String city;
    private String state;
    private String zipCode;
    private String country;
    private String landmark;
    private String deliveryInstructions;
    private Double latitude;
    private Double longitude;
    private Long userId;
    private boolean isDefault;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Computed fields
    public String getFullAddress() {
        StringBuilder address = new StringBuilder();
        
        if (apartment != null && !apartment.trim().isEmpty()) {
            address.append(apartment).append(", ");
        }
        
        address.append(street);
        
        if (landmark != null && !landmark.trim().isEmpty()) {
            address.append(", Near ").append(landmark);
        }
        
        address.append(", ").append(city)
               .append(", ").append(state)
               .append(" - ").append(zipCode)
               .append(", ").append(country);
        
        return address.toString();
    }
    
    public String getShortAddress() {
        StringBuilder address = new StringBuilder();
        
        if (apartment != null && !apartment.trim().isEmpty()) {
            address.append(apartment).append(", ");
        }
        
        address.append(street).append(", ").append(city);
        
        return address.toString();
    }
    
    public boolean hasCoordinates() {
        return latitude != null && longitude != null;
    }
}