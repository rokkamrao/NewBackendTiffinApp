package com.tiffin.membership.service;

import com.tiffin.membership.model.MembershipPlan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.lang.NonNull;

/**
 * Membership notification service
 * Handles all membership-related notifications
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MembershipNotificationService {
    
    /**
     * Send welcome notification for new membership
     */
    public void sendWelcomeNotification(@NonNull Long userId, @NonNull MembershipPlan plan) {
        log.info("Sending welcome notification to user: {} for plan: {}", userId, plan.getName());
        
        try {
            // In real implementation, this would:
            // 1. Send email via email service
            // 2. Send push notification
            // 3. Create in-app notification
            // 4. Send SMS if configured
            
            String message = String.format(
                "Welcome to %s! 🎉\n\n" +
                "Thank you for subscribing to our premium membership. You now have access to:\n" +
                "• %s food discount\n" +
                "• %s delivery benefits\n" +
                "• %sx loyalty points\n" +
                "• Priority customer support\n\n" +
                "Start enjoying your premium experience today!",
                plan.getDisplayName(),
                plan.getFoodDiscount() != null ? plan.getFoodDiscount() + "%" : "Exclusive",
                plan.getFreeDelivery() ? "Free" : (plan.getDeliveryDiscount() != null ? plan.getDeliveryDiscount() + "% off" : "Special"),
                plan.getLoyaltyMultiplier()
            );
            
            // Simulate notification sending
            simulateNotificationSending(userId, "Membership Welcome", message, NotificationType.WELCOME);
            
            log.info("Welcome notification sent successfully to user: {}", userId);
            
        } catch (Exception e) {
            log.error("Failed to send welcome notification to user: {}", userId, e);
        }
    }
    
    /**
     * Send upgrade notification
     */
    public void sendUpgradeNotification(@NonNull Long userId, @NonNull MembershipPlan oldPlan, @NonNull MembershipPlan newPlan) {
        log.info("Sending upgrade notification to user: {} from {} to {}", userId, oldPlan.getName(), newPlan.getName());
        
        try {
            String message = String.format(
                "Membership Upgraded! 🚀\n\n" +
                "You've successfully upgraded from %s to %s.\n\n" +
                "Your new benefits include:\n" +
                "• Enhanced discounts and savings\n" +
                "• Increased loyalty point multiplier\n" +
                "• Priority support and services\n" +
                "• Exclusive early access to new features\n\n" +
                "Enjoy your enhanced premium experience!",
                oldPlan.getDisplayName(),
                newPlan.getDisplayName()
            );
            
            simulateNotificationSending(userId, "Membership Upgraded", message, NotificationType.UPGRADE);
            
            log.info("Upgrade notification sent successfully to user: {}", userId);
            
        } catch (Exception e) {
            log.error("Failed to send upgrade notification to user: {}", userId, e);
        }
    }
    
    /**
     * Send cancellation notification
     */
    public void sendCancellationNotification(@NonNull Long userId, @NonNull MembershipPlan plan) {
        log.info("Sending cancellation notification to user: {} for plan: {}", userId, plan.getName());
        
        try {
            String message = String.format(
                "Membership Cancelled 😢\n\n" +
                "Your %s membership has been cancelled as requested.\n\n" +
                "Your benefits will remain active until the end of your current billing period.\n\n" +
                "We'd love to have you back! Check out our latest plans and offers anytime.\n\n" +
                "Thank you for being part of our premium community.",
                plan.getDisplayName()
            );
            
            simulateNotificationSending(userId, "Membership Cancelled", message, NotificationType.CANCELLATION);
            
            log.info("Cancellation notification sent successfully to user: {}", userId);
            
        } catch (Exception e) {
            log.error("Failed to send cancellation notification to user: {}", userId, e);
        }
    }
    
    /**
     * Send expiry reminder notification
     */
    public void sendExpiryReminder(@NonNull Long userId, @NonNull MembershipPlan plan, int daysRemaining) {
        log.info("Sending expiry reminder to user: {} for plan: {} with {} days remaining", userId, plan.getName(), daysRemaining);
        
        try {
            String message = String.format(
                "Membership Expiring Soon ⏰\n\n" +
                "Your %s membership will expire in %d day(s).\n\n" +
                "Don't miss out on your premium benefits:\n" +
                "• Exclusive discounts\n" +
                "• Free delivery\n" +
                "• Priority support\n" +
                "• Bonus loyalty points\n\n" +
                "Renew now to continue enjoying premium experience!",
                plan.getDisplayName(),
                daysRemaining
            );
            
            simulateNotificationSending(userId, "Membership Expiring Soon", message, NotificationType.EXPIRY_REMINDER);
            
            log.info("Expiry reminder sent successfully to user: {}", userId);
            
        } catch (Exception e) {
            log.error("Failed to send expiry reminder to user: {}", userId, e);
        }
    }
    
    /**
     * Send renewal confirmation
     */
    public void sendRenewalConfirmation(@NonNull Long userId, @NonNull MembershipPlan plan) {
        log.info("Sending renewal confirmation to user: {} for plan: {}", userId, plan.getName());
        
        try {
            String message = String.format(
                "Membership Renewed! 🎊\n\n" +
                "Your %s membership has been automatically renewed.\n\n" +
                "Continue enjoying:\n" +
                "• All premium benefits\n" +
                "• Exclusive discounts and offers\n" +
                "• Priority customer support\n" +
                "• Enhanced loyalty rewards\n\n" +
                "Thank you for staying with us!",
                plan.getDisplayName()
            );
            
            simulateNotificationSending(userId, "Membership Renewed", message, NotificationType.RENEWAL);
            
            log.info("Renewal confirmation sent successfully to user: {}", userId);
            
        } catch (Exception e) {
            log.error("Failed to send renewal confirmation to user: {}", userId, e);
        }
    }
    
    /**
     * Send loyalty tier upgrade notification
     */
    public void sendLoyaltyTierUpgrade(@NonNull Long userId, @NonNull String oldTier, @NonNull String newTier) {
        log.info("Sending loyalty tier upgrade notification to user: {} from {} to {}", userId, oldTier, newTier);
        
        try {
            String message = String.format(
                "Loyalty Tier Upgraded! ⭐\n\n" +
                "Congratulations! You've been upgraded from %s to %s tier.\n\n" +
                "Your new tier benefits:\n" +
                "• Higher loyalty point multiplier\n" +
                "• Exclusive tier-specific offers\n" +
                "• Enhanced customer support\n" +
                "• Special tier rewards\n\n" +
                "Keep earning points to unlock even more rewards!",
                oldTier,
                newTier
            );
            
            simulateNotificationSending(userId, "Loyalty Tier Upgraded", message, NotificationType.LOYALTY_UPGRADE);
            
            log.info("Loyalty tier upgrade notification sent successfully to user: {}", userId);
            
        } catch (Exception e) {
            log.error("Failed to send loyalty tier upgrade notification to user: {}", userId, e);
        }
    }
    
    // ==================== PRIVATE HELPER METHODS ====================
    
    private void simulateNotificationSending(@NonNull Long userId, String title, String message, NotificationType type) {
        // In real implementation, this would:
        // 1. Save notification to database
        // 2. Send via various channels (email, push, SMS)
        // 3. Update user preferences
        // 4. Track delivery status
        
        log.debug("📧 [{}] Notification to user {}: {}", type, userId, title);
        log.debug("📝 Message: {}", message.substring(0, Math.min(100, message.length())) + "...");
        
        // Simulate notification processing time
        try {
            Thread.sleep(100 + (long)(Math.random() * 200)); // 100-300ms
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

/**
 * Notification type enumeration
 */
enum NotificationType {
    WELCOME,
    UPGRADE,
    CANCELLATION,
    EXPIRY_REMINDER,
    RENEWAL,
    LOYALTY_UPGRADE,
    PAYMENT_FAILED,
    BENEFITS_UPDATE
}