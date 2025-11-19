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

/**
 * User loyalty points and rewards system
 */
@Entity
@Table(name = "loyalty_points")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoyaltyPoints {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NonNull
    @Column(nullable = false)
    private Long userId;
    
    @NonNull
    @Builder.Default
    @Column(nullable = false)
    private Integer totalPoints = 0;
    
    @NonNull
    @Builder.Default
    @Column(nullable = false)
    private Integer availablePoints = 0;
    
    @NonNull
    @Builder.Default
    @Column(nullable = false)
    private Integer usedPoints = 0;
    
    @NonNull
    @Builder.Default
    @Column(nullable = false)
    private Integer expiredPoints = 0;
    
    @NonNull
    @Builder.Default
    @Column(nullable = false)
    private Integer lifetimeEarned = 0;
    
    @NonNull
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private LoyaltyTier tier = LoyaltyTier.BRONZE;
    
    @Nullable
    private LocalDateTime nextTierDate; // When user will reach next tier
    
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
     * Add points to user account
     */
    public void addPoints(int points, BigDecimal multiplier) {
        if (points <= 0) return;
        
        // Apply membership multiplier
        int actualPoints = BigDecimal.valueOf(points)
            .multiply(multiplier != null ? multiplier : BigDecimal.ONE)
            .intValue();
        
        this.totalPoints += actualPoints;
        this.availablePoints += actualPoints;
        this.lifetimeEarned += actualPoints;
        
        // Update tier based on lifetime earned points
        updateTier();
        
        this.updatedAt = java.util.Objects.requireNonNull(LocalDateTime.now());
    }
    
    /**
     * Use points for redemption
     */
    public boolean usePoints(int points) {
        if (points <= 0 || availablePoints < points) {
            return false;
        }
        
        this.availablePoints -= points;
        this.usedPoints += points;
        this.updatedAt = java.util.Objects.requireNonNull(LocalDateTime.now());
        
        return true;
    }
    
    /**
     * Expire points (for points that have expiry)
     */
    public void expirePoints(int points) {
        if (points <= 0) return;
        
        int pointsToExpire = Math.min(points, availablePoints);
        this.availablePoints -= pointsToExpire;
        this.expiredPoints += pointsToExpire;
        this.updatedAt = java.util.Objects.requireNonNull(LocalDateTime.now());
    }
    
    /**
     * Update loyalty tier based on lifetime earned points
     */
    private void updateTier() {
        LoyaltyTier newTier = LoyaltyTier.calculateTier(lifetimeEarned);
        if (newTier != this.tier) {
            this.tier = java.util.Objects.requireNonNull(newTier);
            // Calculate next tier date estimation (simplified)
            if (newTier != LoyaltyTier.DIAMOND) {
                this.nextTierDate = LocalDateTime.now().plusDays(30); // Estimated
            } else {
                this.nextTierDate = null; // Already at highest tier
            }
        }
    }
    
    /**
     * Get points needed for next tier
     */
    public int getPointsNeededForNextTier() {
        return tier.getPointsForNextTier(lifetimeEarned);
    }
    
    /**
     * Get progress percentage to next tier
     */
    public double getProgressToNextTier() {
        return tier.getProgressToNextTier(lifetimeEarned);
    }
    
    /**
     * Check if user can redeem points
     */
    public boolean canRedeem(int points) {
        return availablePoints >= points && points >= LoyaltyTier.MIN_REDEMPTION_POINTS;
    }
}