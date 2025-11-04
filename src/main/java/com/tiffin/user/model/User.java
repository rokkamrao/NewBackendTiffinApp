package com.tiffin.user.model;

import com.tiffin.order.model.Order;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * User entity representing a system user
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_user_email", columnList = "email"),
    @Index(name = "idx_user_phone", columnList = "phoneNumber"),
    @Index(name = "idx_user_role", columnList = "role")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String firstName = "";

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String lastName = "";

    @Column(unique = true, length = 15)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Role role = Role.USER;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(nullable = false)
    @Builder.Default
    private boolean emailVerified = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean phoneVerified = false;

    @Column(length = 255)
    private String profileImageUrl;

    @Column(length = 10)
    private String preferredLanguage;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column
    private LocalDateTime lastLoginAt;

    // Relationships
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Address> addresses = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Order> orders = new ArrayList<>();

    // Helper methods
    public String getFullName() {
        if (firstName == null && lastName == null) {
            return "Unknown User";
        }
        return (firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "");
    }

    public String getDisplayName() {
        String fullName = getFullName().trim();
        return fullName.isEmpty() ? email : fullName;
    }

    public Address getDefaultAddress() {
        return addresses.stream()
                .filter(Address::isDefault)
                .findFirst()
                .orElse(null);
    }

    public boolean hasRole(Role requiredRole) {
        return this.role == requiredRole;
    }

    public boolean isAdmin() {
        return hasRole(Role.ADMIN);
    }

    public boolean isDeliveryPerson() {
        return hasRole(Role.DELIVERY_PERSON);
    }

    public void markEmailAsVerified() {
        this.emailVerified = true;
    }

    public void markPhoneAsVerified() {
        this.phoneVerified = true;
    }

    public void updateLastLoginTime() {
        this.lastLoginAt = LocalDateTime.now();
    }

    public void addAddress(Address address) {
        addresses.add(address);
        address.setUser(this);
    }

    public void removeAddress(Address address) {
        addresses.remove(address);
        address.setUser(null);
    }
}