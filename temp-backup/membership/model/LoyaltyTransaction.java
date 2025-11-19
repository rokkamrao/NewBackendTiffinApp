package com.tiffin.membership.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;

/**
 * Points transaction history
 */
@Entity
@Table(name = "loyalty_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoyaltyTransaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NonNull
    @Column(nullable = false)
    private Long userId;
    
    @NonNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;
    
    @NonNull
    @Column(nullable = false)
    private Integer points;
    
    @Nullable
    private String description;
    
    @Nullable
    private Long orderId; // Associated order if applicable
    
    @Nullable
    private String referenceId; // External reference
    
    @Nullable
    private LocalDateTime expiryDate; // For earned points with expiry
    
    @NonNull
    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    public enum TransactionType {
        EARNED_ORDER("Earned from Order"),
        EARNED_REFERRAL("Earned from Referral"),
        EARNED_BONUS("Bonus Points"),
        EARNED_REVIEW("Earned from Review"),
        REDEEMED_DISCOUNT("Redeemed for Discount"),
        REDEEMED_DELIVERY("Redeemed for Free Delivery"),
        EXPIRED("Points Expired"),
        ADJUSTMENT("Manual Adjustment");
        
        private final String description;
        
        TransactionType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}