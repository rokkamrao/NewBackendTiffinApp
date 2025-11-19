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
 * User membership subscription entity
 */
@Entity
@Table(name = "user_memberships")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserMembership {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NonNull
    @Column(nullable = false)
    private Long userId;
    
    @NonNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private MembershipPlan plan;
    
    @NonNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MembershipStatus status;
    
    @NonNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BillingCycle billingCycle;
    
    @NonNull
    @Column(nullable = false)
    private LocalDateTime startDate;
    
    @NonNull
    @Column(nullable = false)
    private LocalDateTime endDate;
    
    @Nullable
    private LocalDateTime nextBillingDate;
    
    @Nullable
    private LocalDateTime cancelledAt;
    
    @Nullable
    private String cancellationReason;
    
    @NonNull
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal paidAmount;
    
    @Nullable
    @Column(precision = 10, scale = 2)
    private BigDecimal discountAmount;
    
    @Nullable
    private String paymentTransactionId;
    
    @Nullable
    private String promoCode;
    
    @Builder.Default
    @Column(nullable = false)
    private Integer ordersUsed = 0;
    
    @Builder.Default
    @Column(nullable = false)
    private Integer deliveriesUsed = 0;
    
    @Builder.Default
    @Column(nullable = false)
    private Integer cancellationsUsed = 0;
    
    @Builder.Default
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalSavings = BigDecimal.ZERO;
    
    @Builder.Default
    @Column(nullable = false)
    private Boolean autoRenewal = true;
    
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
     * Check if membership is currently active
     */
    public boolean isActive() {
        return status == MembershipStatus.ACTIVE && 
               LocalDateTime.now().isBefore(endDate);
    }
    
    /**
     * Check if membership has expired
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(endDate);
    }
    
    /**
     * Check if membership is expiring soon (within 7 days)
     */
    public boolean isExpiringSoon() {
        return isActive() && 
               LocalDateTime.now().plusDays(7).isAfter(endDate);
    }
    
    /**
     * Get days remaining in membership
     */
    public long getDaysRemaining() {
        if (isExpired()) return 0;
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDateTime.now(), endDate);
    }
    
    /**
     * Check if user can place more orders based on plan limits
     */
    public boolean canPlaceOrder() {
        if (!isActive()) return false;
        if (plan.getMaxOrders() == -1) return true; // Unlimited
        return ordersUsed < plan.getMaxOrders();
    }
    
    /**
     * Check if user can get more deliveries based on plan limits
     */
    public boolean canGetDelivery() {
        if (!isActive()) return false;
        if (plan.getMaxDeliveries() == -1) return true; // Unlimited
        return deliveriesUsed < plan.getMaxDeliveries();
    }
    
    /**
     * Check if user can cancel more orders based on plan limits
     */
    public boolean canCancelOrder() {
        if (!isActive()) return false;
        Integer maxCancellations = plan.getMaxCancellations();
        if (maxCancellations == null) return true; // No limit
        return cancellationsUsed < maxCancellations;
    }
    
    /**
     * Calculate membership usage percentage
     */
    public double getUsagePercentage() {
        if (plan.getMaxOrders() == -1) return 0.0; // Unlimited
        return (double) ordersUsed / plan.getMaxOrders() * 100.0;
    }
    
    /**
     * Increment order usage counter
     */
    public void incrementOrderUsage() {
        this.ordersUsed++;
        this.updatedAt = java.util.Objects.requireNonNull(LocalDateTime.now());
    }
    
    /**
     * Increment delivery usage counter
     */
    public void incrementDeliveryUsage() {
        this.deliveriesUsed++;
        this.updatedAt = java.util.Objects.requireNonNull(LocalDateTime.now());
    }
    
    /**
     * Increment cancellation usage counter
     */
    public void incrementCancellationUsage() {
        this.cancellationsUsed++;
        this.updatedAt = java.util.Objects.requireNonNull(LocalDateTime.now());
    }
    
    /**
     * Add savings amount to total savings
     */
    public void addSavings(BigDecimal amount) {
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            this.totalSavings = this.totalSavings.add(amount);
            this.updatedAt = java.util.Objects.requireNonNull(LocalDateTime.now());
        }
    }
}