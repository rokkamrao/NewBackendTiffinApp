package com.tiffin.membership.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * Premium membership plans with different tiers and benefits
 */
@Entity
@Table(name = "membership_plans")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MembershipPlan {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NonNull
    @Column(nullable = false, unique = true)
    private String name;
    
    @NonNull
    @Column(nullable = false)
    private String displayName;
    
    @Nullable
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @NonNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MembershipTier tier;
    
    @NonNull
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monthlyPrice;
    
    @NonNull
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal yearlyPrice;
    
    @Nullable
    @Column(precision = 5, scale = 2)
    private BigDecimal yearlyDiscount; // Percentage discount for yearly subscription
    
    @NonNull
    @Builder.Default
    @Column(nullable = false)
    private Boolean isActive = true;
    
    @NonNull
    @Builder.Default
    @Column(nullable = false)
    private Boolean isFeatured = false;
    
    @Nullable
    private String color; // Hex color for UI
    
    @Nullable
    private String iconUrl;
    
    @NonNull
    @Builder.Default
    @Column(nullable = false)
    private Integer maxOrders = -1; // -1 for unlimited
    
    @NonNull
    @Builder.Default
    @Column(nullable = false)
    private Integer maxDeliveries = -1; // -1 for unlimited
    
    @Nullable
    @Column(precision = 5, scale = 2)
    private BigDecimal deliveryDiscount; // Percentage discount on delivery fees
    
    @Nullable
    @Column(precision = 5, scale = 2)
    private BigDecimal foodDiscount; // Percentage discount on food
    
    @NonNull
    @Builder.Default
    @Column(nullable = false)
    private Boolean freeDelivery = false;
    
    @NonNull
    @Builder.Default
    @Column(nullable = false)
    private Boolean prioritySupport = false;
    
    @NonNull
    @Builder.Default
    @Column(nullable = false)
    private Boolean earlyAccess = false; // Early access to new restaurants/dishes
    
    @NonNull
    @Builder.Default
    @Column(nullable = false)
    private Boolean exclusiveDeals = false;
    
    @NonNull
    @Builder.Default
    @Column(nullable = false)
    private Boolean loyaltyBonusMultiplier = false;
    
    @Nullable
    @Builder.Default
    @Column(precision = 3, scale = 2)
    private BigDecimal loyaltyMultiplier = BigDecimal.ONE; // Default 1x points
    
    @Nullable
    private Integer maxCancellations; // Max order cancellations per month
    
    @Nullable
    private Integer priorityDeliveryMinutes; // Priority delivery time in minutes
    
    @ElementCollection
    @CollectionTable(name = "membership_plan_features", joinColumns = @JoinColumn(name = "plan_id"))
    @Column(name = "feature")
    private Set<String> additionalFeatures;
    
    @NonNull
    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @NonNull
    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = java.util.Objects.requireNonNull(LocalDateTime.now());
    }
    
    /**
     * Calculate discounted yearly price
     */
    public BigDecimal getDiscountedYearlyPrice() {
        if (yearlyDiscount != null && yearlyDiscount.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal discount = java.util.Objects.requireNonNull(yearlyDiscount).divide(BigDecimal.valueOf(100));
            return yearlyPrice.multiply(BigDecimal.ONE.subtract(discount));
        }
        return yearlyPrice;
    }
    
    /**
     * Get savings amount for yearly subscription
     */
    public BigDecimal getYearlySavings() {
        BigDecimal monthlyTotal = monthlyPrice.multiply(BigDecimal.valueOf(12));
        return monthlyTotal.subtract(getDiscountedYearlyPrice());
    }
    
    /**
     * Check if plan includes specific feature
     */
    public boolean hasFeature(String feature) {
        return additionalFeatures != null && additionalFeatures.contains(feature);
    }
}