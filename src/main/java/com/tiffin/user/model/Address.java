package com.tiffin.user.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Address entity representing user addresses
 */
@Entity
@Table(name = "addresses", indexes = {
    @Index(name = "idx_address_user", columnList = "user_id"),
    @Index(name = "idx_address_default", columnList = "user_id, isDefault")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String label; // Home, Work, Other

    @Column(nullable = false, length = 255)
    private String street;

    @Column(length = 100)
    private String apartment;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false, length = 100)
    private String state;

    @Column(nullable = false, length = 20)
    private String zipCode;

    @Builder.Default
    @Column(nullable = false, length = 100)
    private String country = "India";

    @Column(length = 500)
    private String landmark;

    @Column(length = 500)
    private String deliveryInstructions;

    // Geographic coordinates for delivery optimization
    @Column(precision = 10, scale = 8)
    private Double latitude;

    @Column(precision = 11, scale = 8)
    private Double longitude;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder.Default
    @Column(nullable = false)
    private boolean isDefault = false;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Helper methods
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

    public void setCoordinates(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public void markAsDefault() {
        this.isDefault = true;
    }

    public void unmarkAsDefault() {
        this.isDefault = false;
    }

    public void deactivate() {
        this.active = false;
    }

    public void activate() {
        this.active = true;
    }

    public boolean isValidForDelivery() {
        return active && 
               street != null && !street.trim().isEmpty() &&
               city != null && !city.trim().isEmpty() &&
               state != null && !state.trim().isEmpty() &&
               zipCode != null && !zipCode.trim().isEmpty();
    }

    @Override
    public String toString() {
        return String.format("%s: %s", label, getShortAddress());
    }
}