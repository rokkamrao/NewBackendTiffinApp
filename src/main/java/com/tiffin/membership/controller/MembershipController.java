package com.tiffin.membership.controller;

import com.tiffin.membership.model.*;
import com.tiffin.membership.service.MembershipService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.access.prepost.PreAuthorize;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

/**
 * Premium membership REST API controller
 * Handles subscription management, upgrades, and benefits
 */
@RestController
@RequestMapping("/membership")
@RequiredArgsConstructor
@Slf4j
public class MembershipController {
    
    private final MembershipService membershipService;
    
    // ==================== MEMBERSHIP PLANS ====================
    
    /**
     * Get all active membership plans
     */
    @GetMapping("/plans")
    public ResponseEntity<List<MembershipPlan>> getPlans() {
        log.debug("Fetching all membership plans");
        List<MembershipPlan> plans = membershipService.getActivePlans();
        return ResponseEntity.ok(plans);
    }
    
    /**
     * Get featured membership plans
     */
    @GetMapping("/plans/featured")
    public ResponseEntity<List<MembershipPlan>> getFeaturedPlans() {
        log.debug("Fetching featured membership plans");
        List<MembershipPlan> plans = membershipService.getFeaturedPlans();
        return ResponseEntity.ok(plans);
    }
    
    /**
     * Get plan comparison data
     */
    @GetMapping("/plans/comparison")
    public ResponseEntity<List<MembershipPlan>> getPlansComparison() {
        log.debug("Fetching plans for comparison");
        List<MembershipPlan> plans = membershipService.getPlansForComparison();
        return ResponseEntity.ok(plans);
    }
    
    /**
     * Get specific plan details
     */
    @GetMapping("/plans/{planId}")
    public ResponseEntity<MembershipPlan> getPlan(@PathVariable @NonNull Long planId) {
        log.debug("Fetching plan details for ID: {}", planId);
        Optional<MembershipPlan> plan = membershipService.getPlanById(planId);
        return plan.map(ResponseEntity::ok)
                  .orElse(ResponseEntity.notFound().build());
    }
    
    // ==================== USER MEMBERSHIP ====================
    
    /**
     * Get current user's active membership
     */
    @GetMapping("/current")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserMembership> getCurrentMembership() {
        // In real implementation, get user ID from JWT token/security context
        Long userId = getCurrentUserId();
        log.debug("Fetching current membership for user: {}", userId);
        
        Optional<UserMembership> membership = membershipService.getCurrentMembership(userId);
        return membership.map(ResponseEntity::ok)
                        .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Get user's membership history
     */
    @GetMapping("/history")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<UserMembership>> getMembershipHistory() {
        Long userId = getCurrentUserId();
        log.debug("Fetching membership history for user: {}", userId);
        
        List<UserMembership> history = membershipService.getMembershipHistory(userId);
        return ResponseEntity.ok(history);
    }
    
    /**
     * Subscribe to a membership plan
     */
    @PostMapping("/subscribe")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserMembership> subscribe(@RequestBody @Valid SubscriptionRequest request) {
        Long userId = getCurrentUserId();
        log.info("User {} subscribing to plan {} with billing cycle {}", userId, request.getPlanId(), request.getBillingCycle());
        
        try {
            UserMembership membership = membershipService.subscribeUser(
                userId, 
                request.getPlanId(), 
                request.getBillingCycle(), 
                request.getPromoCode()
            );
            return ResponseEntity.ok(membership);
        } catch (Exception e) {
            log.error("Subscription failed for user: {} plan: {}", userId, request.getPlanId(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Upgrade membership plan
     */
    @PostMapping("/upgrade")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserMembership> upgrade(@RequestBody @Valid UpgradeRequest request) {
        Long userId = getCurrentUserId();
        log.info("User {} upgrading to plan {}", userId, request.getNewPlanId());
        
        try {
            UserMembership membership = membershipService.upgradeMembership(userId, request.getNewPlanId());
            return ResponseEntity.ok(membership);
        } catch (Exception e) {
            log.error("Upgrade failed for user: {} plan: {}", userId, request.getNewPlanId(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Cancel membership
     */
    @PostMapping("/cancel")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> cancelMembership(@RequestBody @Valid CancellationRequest request) {
        Long userId = getCurrentUserId();
        log.info("User {} cancelling membership with reason: {}", userId, request.getReason());
        
        try {
            // First get current membership to get ID
            Optional<UserMembership> membershipOpt = membershipService.getCurrentMembership(userId);
            if (membershipOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            membershipService.cancelMembership(membershipOpt.get().getId(), request.getReason());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Cancellation failed for user: {}", userId, e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    // ==================== BENEFITS & USAGE ====================
    
    /**
     * Get membership benefits for order
     */
    @PostMapping("/benefits/calculate")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Object> calculateBenefits(@RequestBody @Valid BenefitsRequest request) {
        Long userId = getCurrentUserId();
        log.debug("Calculating benefits for user: {} order amount: {}", userId, request.getOrderAmount());
        
        Object benefits = membershipService.calculateOrderBenefits(userId, request.getOrderAmount());
        return ResponseEntity.ok(benefits);
    }
    
    /**
     * Record order usage
     */
    @PostMapping("/usage/order")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> recordOrderUsage(@RequestBody @Valid UsageRequest request) {
        Long userId = getCurrentUserId();
        log.debug("Recording order usage for user: {}", userId);
        
        membershipService.recordOrderUsage(userId, request.getOrderAmount(), request.getSavingsAmount());
        return ResponseEntity.ok().build();
    }
    
    // ==================== LOYALTY POINTS ====================
    
    /**
     * Get user's loyalty points
     */
    @GetMapping("/loyalty")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<LoyaltyPoints> getLoyaltyPoints() {
        Long userId = getCurrentUserId();
        log.debug("Fetching loyalty points for user: {}", userId);
        
        LoyaltyPoints points = membershipService.getUserLoyaltyPoints(userId);
        return ResponseEntity.ok(points);
    }
    
    /**
     * Redeem loyalty points
     */
    @PostMapping("/loyalty/redeem")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<RedemptionResponse> redeemPoints(@RequestBody @Valid RedemptionRequest request) {
        Long userId = getCurrentUserId();
        log.info("User {} redeeming {} points for: {}", userId, request.getPoints(), request.getDescription());
        
        boolean success = membershipService.redeemLoyaltyPoints(userId, request.getPoints(), request.getDescription());
        
        RedemptionResponse response = RedemptionResponse.builder()
            .success(success)
            .message(success ? "Points redeemed successfully" : "Insufficient points or invalid redemption")
            .build();
        
        return ResponseEntity.ok(response);
    }
    
    // ==================== ADMIN ENDPOINTS ====================
    
    /**
     * Get membership analytics (Admin only)
     */
    @GetMapping("/admin/analytics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> getMembershipAnalytics() {
        log.debug("Fetching membership analytics");
        // Implementation would include various membership metrics
        return ResponseEntity.ok().build();
    }
    
    // ==================== PRIVATE HELPER METHODS ====================
    
    @NonNull
    private Long getCurrentUserId() {
        // In real implementation, extract from JWT token or security context
        // For now, return a mock user ID
        return 1L;
    }
}

// ==================== REQUEST/RESPONSE DTOs ====================

@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
class SubscriptionRequest {
    @NonNull
    private Long planId;
    
    @NonNull
    private BillingCycle billingCycle;
    
    @Nullable
    private String promoCode;
}

@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
class UpgradeRequest {
    @NonNull
    private Long newPlanId;
}

@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
class CancellationRequest {
    @Nullable
    private String reason;
}

@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
class BenefitsRequest {
    @NonNull
    private java.math.BigDecimal orderAmount;
}

@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
class UsageRequest {
    @NonNull
    private java.math.BigDecimal orderAmount;
    
    @NonNull
    private java.math.BigDecimal savingsAmount;
}

@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
class RedemptionRequest {
    @NonNull
    private Integer points;
    
    @Nullable
    private String description;
}

@lombok.Data
@lombok.Builder
class RedemptionResponse {
    private boolean success;
    private String message;
}