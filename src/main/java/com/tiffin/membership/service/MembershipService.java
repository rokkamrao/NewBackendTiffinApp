package com.tiffin.membership.service;

import com.tiffin.membership.model.*;
import com.tiffin.membership.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Premium membership service handling subscription management,
 * benefits, upgrades, and loyalty points integration
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MembershipService {
    
    private final MembershipPlanRepository planRepository;
    private final UserMembershipRepository membershipRepository;
    private final LoyaltyPointsRepository loyaltyRepository;
    private final MembershipPaymentService paymentService;
    private final MembershipNotificationService notificationService;
    
    // ==================== PLAN MANAGEMENT ====================
    
    /**
     * Get all active membership plans
     */
    @Transactional(readOnly = true)
    public List<MembershipPlan> getActivePlans() {
        log.debug("Fetching all active membership plans");
        return planRepository.findByIsActiveTrueOrderByTier();
    }
    
    /**
     * Get featured membership plans
     */
    @Transactional(readOnly = true)
    public List<MembershipPlan> getFeaturedPlans() {
        log.debug("Fetching featured membership plans");
        return planRepository.findByIsFeaturedTrueAndIsActiveTrueOrderByTier();
    }
    
    /**
     * Get plan by ID
     */
    @Transactional(readOnly = true)
    public Optional<MembershipPlan> getPlanById(@NonNull Long planId) {
        log.debug("Fetching membership plan with ID: {}", planId);
        return planRepository.findById(planId);
    }
    
    /**
     * Get plan comparison data
     */
    @Transactional(readOnly = true)
    public List<MembershipPlan> getPlansForComparison() {
        log.debug("Fetching plans for comparison");
        return planRepository.findAllActiveOrderedByPrice();
    }
    
    // ==================== USER MEMBERSHIP MANAGEMENT ====================
    
    /**
     * Get user's current active membership
     */
    @Transactional(readOnly = true)
    public Optional<UserMembership> getCurrentMembership(@NonNull Long userId) {
        log.debug("Fetching current membership for user: {}", userId);
        return membershipRepository.findActiveByUserId(userId);
    }
    
    /**
     * Get user's membership history
     */
    @Transactional(readOnly = true)
    public List<UserMembership> getMembershipHistory(@NonNull Long userId) {
        log.debug("Fetching membership history for user: {}", userId);
        return membershipRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    /**
     * Subscribe user to a membership plan
     */
    public UserMembership subscribeUser(@NonNull Long userId, @NonNull Long planId, 
                                       @NonNull BillingCycle billingCycle, 
                                       @Nullable String promoCode) {
        log.info("Subscribing user {} to plan {} with billing cycle {}", userId, planId, billingCycle);
        
        // Get the plan
        MembershipPlan plan = planRepository.findById(planId)
            .orElseThrow(() -> new IllegalArgumentException("Membership plan not found: " + planId));
        
        if (!plan.getIsActive()) {
            throw new IllegalStateException("Membership plan is not active: " + planId);
        }
        
        // Cancel existing active membership if any
        getCurrentMembership(userId).ifPresent(existing -> {
            log.info("Cancelling existing membership for user: {}", userId);
            cancelMembership(existing.getId(), "Upgraded to new plan");
        });
        
        // Calculate pricing
        BigDecimal price = billingCycle == BillingCycle.YEARLY ? 
            plan.getDiscountedYearlyPrice() : plan.getMonthlyPrice();
        
        // Apply promo code discount if applicable
        BigDecimal finalPrice = applyPromoCodeDiscount(price, promoCode);
        BigDecimal discountAmount = price.subtract(finalPrice);
        
        // Calculate subscription period
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = billingCycle == BillingCycle.YEARLY ? 
            startDate.plusYears(1) : startDate.plusMonths(1);
        LocalDateTime nextBillingDate = endDate.minusDays(3); // Reminder 3 days before
        
        // Create membership record
        UserMembership membership = UserMembership.builder()
            .userId(userId)
            .plan(plan)
            .status(MembershipStatus.PENDING_PAYMENT)
            .billingCycle(billingCycle)
            .startDate(startDate)
            .endDate(endDate)
            .nextBillingDate(nextBillingDate)
            .paidAmount(finalPrice)
            .discountAmount(discountAmount.compareTo(BigDecimal.ZERO) > 0 ? discountAmount : null)
            .promoCode(promoCode)
            .build();
        
        membership = membershipRepository.save(membership);
        log.info("Created membership record with ID: {}", membership.getId());
        
        // Process payment
        String transactionId = paymentService.processPayment(userId, finalPrice, 
            "Membership subscription: " + plan.getDisplayName());
        
        // Update membership with payment details
        membership.setPaymentTransactionId(transactionId);
        membership.setStatus(MembershipStatus.ACTIVE);
        membership = membershipRepository.save(membership);
        
        // Award loyalty points for subscription
        awardLoyaltyPoints(userId, finalPrice.intValue(), "Membership subscription");
        
        // Send welcome notification
        notificationService.sendWelcomeNotification(userId, plan);
        
        log.info("Successfully subscribed user {} to plan {}", userId, plan.getName());
        return membership;
    }
    
    /**
     * Cancel user membership
     */
    public void cancelMembership(@NonNull Long membershipId, @Nullable String reason) {
        log.info("Cancelling membership: {} with reason: {}", membershipId, reason);
        
        UserMembership membership = membershipRepository.findById(membershipId)
            .orElseThrow(() -> new IllegalArgumentException("Membership not found: " + membershipId));
        
        if (membership.getStatus() != MembershipStatus.ACTIVE) {
            throw new IllegalStateException("Can only cancel active memberships");
        }
        
        membership.setStatus(MembershipStatus.CANCELLED);
        membership.setCancelledAt(LocalDateTime.now());
        membership.setCancellationReason(reason);
        membership.setAutoRenewal(false);
        
        membershipRepository.save(membership);
        
        // Send cancellation notification
        notificationService.sendCancellationNotification(membership.getUserId(), membership.getPlan());
        
        log.info("Successfully cancelled membership: {}", membershipId);
    }
    
    /**
     * Upgrade user membership
     */
    public UserMembership upgradeMembership(@NonNull Long userId, @NonNull Long newPlanId) {
        log.info("Upgrading membership for user {} to plan {}", userId, newPlanId);
        
        UserMembership currentMembership = getCurrentMembership(userId)
            .orElseThrow(() -> new IllegalStateException("No active membership found for user: " + userId));
        
        MembershipPlan newPlan = planRepository.findById(newPlanId)
            .orElseThrow(() -> new IllegalArgumentException("Membership plan not found: " + newPlanId));
        
        MembershipPlan currentPlan = currentMembership.getPlan();
        
        // Check if it's actually an upgrade
        if (!newPlan.getTier().isHigherThan(currentPlan.getTier())) {
            throw new IllegalArgumentException("Can only upgrade to higher tier plans");
        }
        
        // Calculate prorated pricing
        BigDecimal proratedAmount = calculateProratedUpgrade(currentMembership, newPlan);
        
        // Process upgrade payment
        String transactionId = paymentService.processPayment(userId, proratedAmount,
            "Membership upgrade: " + currentPlan.getDisplayName() + " to " + newPlan.getDisplayName());
        
        // Update current membership
        currentMembership.setPlan(newPlan);
        currentMembership.setPaidAmount(currentMembership.getPaidAmount().add(proratedAmount));
        currentMembership.setPaymentTransactionId(transactionId);
        
        UserMembership upgradedMembership = membershipRepository.save(currentMembership);
        
        // Award bonus loyalty points for upgrade
        awardLoyaltyPoints(userId, proratedAmount.intValue() * 2, "Membership upgrade bonus");
        
        // Send upgrade notification
        notificationService.sendUpgradeNotification(userId, currentPlan, newPlan);
        
        log.info("Successfully upgraded membership for user {} to plan {}", userId, newPlan.getName());
        return upgradedMembership;
    }
    
    // ==================== BENEFITS & USAGE ====================
    
    /**
     * Apply membership benefits to order
     */
    public MembershipBenefits calculateOrderBenefits(@NonNull Long userId, @NonNull BigDecimal orderAmount) {
        log.debug("Calculating order benefits for user: {} with order amount: {}", userId, orderAmount);
        
        Optional<UserMembership> membershipOpt = getCurrentMembership(userId);
        if (membershipOpt.isEmpty()) {
            return MembershipBenefits.builder()
                .hasActiveMembership(false)
                .build();
        }
        
        UserMembership membership = membershipOpt.get();
        MembershipPlan plan = membership.getPlan();
        
        BigDecimal foodDiscount = BigDecimal.ZERO;
        BigDecimal deliveryDiscount = BigDecimal.ZERO;
        boolean freeDelivery = plan.getFreeDelivery();
        
        // Calculate food discount
        if (plan.getFoodDiscount() != null) {
            foodDiscount = orderAmount.multiply(plan.getFoodDiscount().divide(BigDecimal.valueOf(100)));
        }
        
        // Calculate delivery discount
        if (plan.getDeliveryDiscount() != null && !freeDelivery) {
            deliveryDiscount = BigDecimal.valueOf(50).multiply(plan.getDeliveryDiscount().divide(BigDecimal.valueOf(100))); // Assuming ₹50 delivery fee
        }
        
        return MembershipBenefits.builder()
            .hasActiveMembership(true)
            .planName(plan.getDisplayName())
            .tier(plan.getTier())
            .foodDiscount(foodDiscount)
            .deliveryDiscount(deliveryDiscount)
            .freeDelivery(freeDelivery)
            .prioritySupport(plan.getPrioritySupport())
            .loyaltyMultiplier(plan.getLoyaltyMultiplier())
            .build();
    }
    
    /**
     * Record order usage for membership
     */
    public void recordOrderUsage(@NonNull Long userId, @NonNull BigDecimal orderAmount, @NonNull BigDecimal savingsAmount) {
        getCurrentMembership(userId).ifPresent(membership -> {
            membership.incrementOrderUsage();
            membership.addSavings(savingsAmount);
            membershipRepository.save(membership);
            
            // Award loyalty points
            BigDecimal multiplier = membership.getPlan().getLoyaltyMultiplier();
            int basePoints = orderAmount.divide(BigDecimal.valueOf(10)).intValue(); // 1 point per ₹10
            awardLoyaltyPoints(userId, basePoints, multiplier, "Order completion");
            
            log.debug("Recorded order usage for user: {}, savings: {}", userId, savingsAmount);
        });
    }
    
    // ==================== LOYALTY POINTS ====================
    
    /**
     * Get user's loyalty points
     */
    @Transactional(readOnly = true)
    public LoyaltyPoints getUserLoyaltyPoints(@NonNull Long userId) {
        return loyaltyRepository.findByUserId(userId)
            .orElseGet(() -> createInitialLoyaltyPoints(userId));
    }
    
    /**
     * Award loyalty points to user
     */
    public void awardLoyaltyPoints(@NonNull Long userId, int points, @Nullable String description) {
        BigDecimal multiplier = getCurrentMembership(userId)
            .map(membership -> membership.getPlan().getLoyaltyMultiplier())
            .orElse(BigDecimal.ONE);
        
        awardLoyaltyPoints(userId, points, multiplier, description);
    }
    
    /**
     * Award loyalty points with multiplier
     */
    public void awardLoyaltyPoints(@NonNull Long userId, int basePoints, @NonNull BigDecimal multiplier, @Nullable String description) {
        LoyaltyPoints loyaltyPoints = getUserLoyaltyPoints(userId);
        loyaltyPoints.addPoints(basePoints, multiplier);
        loyaltyRepository.save(loyaltyPoints);
        
        log.debug("Awarded {} base points ({}x multiplier) to user: {} for: {}", 
                 basePoints, multiplier, userId, description);
    }
    
    /**
     * Redeem loyalty points
     */
    public boolean redeemLoyaltyPoints(@NonNull Long userId, int points, @Nullable String description) {
        LoyaltyPoints loyaltyPoints = getUserLoyaltyPoints(userId);
        
        if (!loyaltyPoints.canRedeem(points)) {
            log.warn("User {} cannot redeem {} points. Available: {}", userId, points, loyaltyPoints.getAvailablePoints());
            return false;
        }
        
        boolean success = loyaltyPoints.usePoints(points);
        if (success) {
            loyaltyRepository.save(loyaltyPoints);
            log.info("User {} redeemed {} points for: {}", userId, points, description);
        }
        
        return success;
    }
    
    // ==================== PRIVATE HELPER METHODS ====================
    
    private LoyaltyPoints createInitialLoyaltyPoints(@NonNull Long userId) {
        LoyaltyPoints loyaltyPoints = LoyaltyPoints.builder()
            .userId(userId)
            .build();
        return loyaltyRepository.save(loyaltyPoints);
    }
    
    private BigDecimal applyPromoCodeDiscount(@NonNull BigDecimal price, @Nullable String promoCode) {
        if (promoCode == null || promoCode.trim().isEmpty()) {
            return price;
        }
        
        // Simple promo code logic - in real implementation, this would check a promo codes table
        switch (promoCode.toUpperCase()) {
            case "WELCOME10":
                return price.multiply(BigDecimal.valueOf(0.9)); // 10% discount
            case "NEWUSER20":
                return price.multiply(BigDecimal.valueOf(0.8)); // 20% discount
            case "PREMIUM50":
                return price.multiply(BigDecimal.valueOf(0.5)); // 50% discount
            default:
                log.warn("Invalid promo code: {}", promoCode);
                return price;
        }
    }
    
    private BigDecimal calculateProratedUpgrade(@NonNull UserMembership currentMembership, @NonNull MembershipPlan newPlan) {
        // Calculate remaining days in current subscription
        long remainingDays = java.time.temporal.ChronoUnit.DAYS.between(LocalDateTime.now(), currentMembership.getEndDate());
        
        BigDecimal currentDailyRate = currentMembership.getBillingCycle() == BillingCycle.YEARLY ?
            currentMembership.getPlan().getYearlyPrice().divide(BigDecimal.valueOf(365), 2, java.math.RoundingMode.HALF_UP) :
            currentMembership.getPlan().getMonthlyPrice().divide(BigDecimal.valueOf(30), 2, java.math.RoundingMode.HALF_UP);
            
        BigDecimal newDailyRate = currentMembership.getBillingCycle() == BillingCycle.YEARLY ?
            newPlan.getYearlyPrice().divide(BigDecimal.valueOf(365), 2, java.math.RoundingMode.HALF_UP) :
            newPlan.getMonthlyPrice().divide(BigDecimal.valueOf(30), 2, java.math.RoundingMode.HALF_UP);
        
        BigDecimal dailyDifference = newDailyRate.subtract(currentDailyRate);
        return dailyDifference.multiply(BigDecimal.valueOf(remainingDays));
    }
}

/**
 * DTO for membership benefits calculation
 */
@lombok.Data
@lombok.Builder
class MembershipBenefits {
    private boolean hasActiveMembership;
    private String planName;
    private MembershipTier tier;
    private BigDecimal foodDiscount;
    private BigDecimal deliveryDiscount;
    private boolean freeDelivery;
    private boolean prioritySupport;
    private BigDecimal loyaltyMultiplier;
}