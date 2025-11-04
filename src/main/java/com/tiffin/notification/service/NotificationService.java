package com.tiffin.notification.service;

import com.tiffin.notification.dto.NotificationMessage;
import com.tiffin.notification.model.NotificationType;
import com.tiffin.notification.model.NotificationPriority;
import com.tiffin.payment.model.Payment;
import com.tiffin.order.model.Order;
import com.tiffin.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    
    private final SimpMessagingTemplate messagingTemplate;
    
    @Value("${app.notification.enabled:true}")
    private boolean notificationEnabled;
    
    @Value("${app.notification.email.enabled:true}")
    private boolean emailEnabled;
    
    @Value("${app.notification.sms.enabled:false}")
    private boolean smsEnabled;
    
    @Value("${app.notification.push.enabled:true}")
    private boolean pushEnabled;
    
    // Email Notifications
    @Async
    public CompletableFuture<Void> sendOrderConfirmation(String userEmail, String orderId) {
        if (!notificationEnabled || !emailEnabled) {
            return CompletableFuture.completedFuture(null);
        }
        
        log.info("Sending order confirmation email to {} for order {}", userEmail, orderId);
        try {
            // TODO: Implement actual email sending logic with template
            Map<String, Object> templateData = new HashMap<>();
            templateData.put("orderId", orderId);
            templateData.put("timestamp", LocalDateTime.now());
            
            // Simulate email sending
            Thread.sleep(100);
            log.info("Order confirmation email sent successfully to {}", userEmail);
            
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            log.error("Failed to send order confirmation email to {}: {}", userEmail, e.getMessage());
            throw new RuntimeException("Email sending failed", e);
        }
    }
    
    @Async
    public CompletableFuture<Void> sendOrderStatusUpdate(String userEmail, String orderId, String status) {
        if (!notificationEnabled || !emailEnabled) {
            return CompletableFuture.completedFuture(null);
        }
        
        log.info("Sending order status update email to {} for order {} with status {}", userEmail, orderId, status);
        try {
            Map<String, Object> templateData = new HashMap<>();
            templateData.put("orderId", orderId);
            templateData.put("status", status);
            templateData.put("timestamp", LocalDateTime.now());
            
            Thread.sleep(100);
            log.info("Order status update email sent successfully to {}", userEmail);
            
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            log.error("Failed to send order status email to {}: {}", userEmail, e.getMessage());
            throw new RuntimeException("Email sending failed", e);
        }
    }
    
    @Async
    public CompletableFuture<Void> sendPaymentConfirmation(String userEmail, String paymentId, String amount) {
        if (!notificationEnabled || !emailEnabled) {
            return CompletableFuture.completedFuture(null);
        }
        
        log.info("Sending payment confirmation email to {} for payment {} with amount {}", userEmail, paymentId, amount);
        try {
            Map<String, Object> templateData = new HashMap<>();
            templateData.put("paymentId", paymentId);
            templateData.put("amount", amount);
            templateData.put("timestamp", LocalDateTime.now());
            
            Thread.sleep(100);
            log.info("Payment confirmation email sent successfully to {}", userEmail);
            
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            log.error("Failed to send payment confirmation email to {}: {}", userEmail, e.getMessage());
            throw new RuntimeException("Email sending failed", e);
        }
    }
    
    @Async
    public CompletableFuture<Void> sendPaymentFailure(String userEmail, String paymentId, String reason) {
        if (!notificationEnabled || !emailEnabled) {
            return CompletableFuture.completedFuture(null);
        }
        
        log.warn("Sending payment failure notification to {} for payment {} with reason: {}", userEmail, paymentId, reason);
        try {
            Map<String, Object> templateData = new HashMap<>();
            templateData.put("paymentId", paymentId);
            templateData.put("reason", reason);
            templateData.put("timestamp", LocalDateTime.now());
            
            Thread.sleep(100);
            log.info("Payment failure email sent successfully to {}", userEmail);
            
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            log.error("Failed to send payment failure email to {}: {}", userEmail, e.getMessage());
            throw new RuntimeException("Email sending failed", e);
        }
    }
    
    @Async
    public CompletableFuture<Void> sendWelcomeEmail(String userEmail, String userName) {
        if (!notificationEnabled || !emailEnabled) {
            return CompletableFuture.completedFuture(null);
        }
        
        log.info("Sending welcome email to {} for user {}", userEmail, userName);
        try {
            Map<String, Object> templateData = new HashMap<>();
            templateData.put("userName", userName);
            templateData.put("timestamp", LocalDateTime.now());
            
            Thread.sleep(100);
            log.info("Welcome email sent successfully to {}", userEmail);
            
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            log.error("Failed to send welcome email to {}: {}", userEmail, e.getMessage());
            throw new RuntimeException("Email sending failed", e);
        }
    }
    
    @Async
    public CompletableFuture<Void> sendPasswordResetEmail(String userEmail, String resetToken) {
        if (!notificationEnabled || !emailEnabled) {
            return CompletableFuture.completedFuture(null);
        }
        
        log.info("Sending password reset email to {} with token", userEmail);
        try {
            Map<String, Object> templateData = new HashMap<>();
            templateData.put("resetToken", resetToken);
            templateData.put("timestamp", LocalDateTime.now());
            templateData.put("expiryTime", LocalDateTime.now().plusHours(1));
            
            Thread.sleep(100);
            log.info("Password reset email sent successfully to {}", userEmail);
            
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}: {}", userEmail, e.getMessage());
            throw new RuntimeException("Email sending failed", e);
        }
    }
    
    // Enhanced notification methods with Payment and Order objects
    public void sendPaymentSuccessNotification(String userEmail, Payment payment) {
        if (!notificationEnabled) return;
        
        try {
            // Send email notification
            sendPaymentConfirmation(userEmail, payment.getId().toString(), payment.getAmount().toString());
            
            // Send real-time notification - use userId from payment
            sendRealTimeNotification(Long.valueOf(payment.getUserId()), 
                "Payment Successful", 
                "Your payment of ₹" + payment.getAmount() + " has been processed successfully.",
                "PAYMENT_SUCCESS");
                
        } catch (Exception e) {
            log.error("Failed to send payment success notification: {}", e.getMessage());
        }
    }
    
    public void sendPaymentFailureNotification(String userEmail, Payment payment) {
        if (!notificationEnabled) return;
        
        try {
            // Send email notification
            sendPaymentFailure(userEmail, payment.getId().toString(), "Payment processing failed");
            
            // Send real-time notification - use userId from payment
            sendRealTimeNotification(Long.valueOf(payment.getUserId()),
                "Payment Failed",
                "Your payment for order #" + payment.getOrderId() + " could not be processed. Please try again.",
                "PAYMENT_FAILED");
                
        } catch (Exception e) {
            log.error("Failed to send payment failure notification: {}", e.getMessage());
        }
    }
    
    // Real-time WebSocket notifications
    public void sendRealTimeNotification(Long userId, String title, String message, String typeString) {
        if (!notificationEnabled || !pushEnabled) return;
        
        try {
            // Convert string to enum if possible, otherwise use SYSTEM_ANNOUNCEMENT
            NotificationType notificationType;
            try {
                notificationType = NotificationType.valueOf(typeString);
            } catch (IllegalArgumentException e) {
                notificationType = NotificationType.SYSTEM_ANNOUNCEMENT;
            }
            
            NotificationMessage notification = NotificationMessage.builder()
                .title(title)
                .message(message)
                .type(notificationType)
                .createdAt(LocalDateTime.now())
                .userId(userId)
                .priority(NotificationPriority.NORMAL)
                .readStatus(false)
                .build();
                
            messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/notifications",
                notification
            );
            
            log.info("Real-time notification sent to user {}: {}", userId, title);
        } catch (Exception e) {
            log.error("Failed to send real-time notification to user {}: {}", userId, e.getMessage());
        }
    }
    
    // Order notifications
    public void sendOrderCreatedNotification(Order order) {
        if (!notificationEnabled) return;
        
        try {
            User user = order.getUser();
            
            // Email notification
            sendOrderConfirmation(user.getEmail(), order.getId().toString());
            
            // Real-time notification
            sendRealTimeNotification(user.getId(),
                "Order Confirmed",
                "Your order #" + order.getId() + " has been confirmed and is being prepared.",
                "ORDER_CONFIRMED");
                
        } catch (Exception e) {
            log.error("Failed to send order created notification: {}", e.getMessage());
        }
    }
    
    public void sendOrderStatusNotification(Order order, String newStatus) {
        if (!notificationEnabled) return;
        
        try {
            User user = order.getUser();
            
            // Email notification
            sendOrderStatusUpdate(user.getEmail(), order.getId().toString(), newStatus);
            
            // Real-time notification
            String message = getStatusMessage(newStatus, order.getId().toString());
            sendRealTimeNotification(user.getId(),
                "Order Update",
                message,
                "ORDER_STATUS_UPDATE");
                
        } catch (Exception e) {
            log.error("Failed to send order status notification: {}", e.getMessage());
        }
    }
    
    // Delivery notifications
    public void sendDeliveryAssignedNotification(Order order, String deliveryPersonName) {
        if (!notificationEnabled) return;
        
        try {
            sendRealTimeNotification(order.getUser().getId(),
                "Delivery Assigned",
                "Your order #" + order.getId() + " has been assigned to " + deliveryPersonName + " for delivery.",
                "DELIVERY_ASSIGNED");
                
        } catch (Exception e) {
            log.error("Failed to send delivery assigned notification: {}", e.getMessage());
        }
    }
    
    public void sendDeliveryLocationUpdate(Order order, Double latitude, Double longitude, Integer estimatedMinutes) {
        if (!notificationEnabled) return;
        
        try {
            Map<String, Object> locationData = new HashMap<>();
            locationData.put("orderId", order.getId());
            locationData.put("latitude", latitude);
            locationData.put("longitude", longitude);
            locationData.put("estimatedMinutes", estimatedMinutes);
            
            messagingTemplate.convertAndSendToUser(
                order.getUser().getId().toString(),
                "/queue/delivery-tracking",
                locationData
            );
            
            log.info("Delivery location update sent for order {}", order.getId());
        } catch (Exception e) {
            log.error("Failed to send delivery location update: {}", e.getMessage());
        }
    }
    
    // SMS notifications (placeholder)
    @Async
    public CompletableFuture<Void> sendSMSNotification(String phoneNumber, String message) {
        if (!notificationEnabled || !smsEnabled) {
            return CompletableFuture.completedFuture(null);
        }
        
        log.info("Sending SMS to {}: {}", maskPhoneNumber(phoneNumber), message);
        try {
            // TODO: Implement SMS gateway integration
            Thread.sleep(50);
            log.info("SMS sent successfully to {}", maskPhoneNumber(phoneNumber));
            
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            log.error("Failed to send SMS to {}: {}", phoneNumber, e.getMessage());
            throw new RuntimeException("SMS sending failed", e);
        }
    }
    
    // Utility methods
    private String getStatusMessage(String status, String orderId) {
        return switch (status.toUpperCase()) {
            case "PREPARING" -> "Your order #" + orderId + " is being prepared by the restaurant.";
            case "READY_FOR_PICKUP" -> "Your order #" + orderId + " is ready for pickup!";
            case "OUT_FOR_DELIVERY" -> "Your order #" + orderId + " is out for delivery.";
            case "DELIVERED" -> "Your order #" + orderId + " has been delivered. Enjoy your meal!";
            case "CANCELLED" -> "Your order #" + orderId + " has been cancelled.";
            default -> "Your order #" + orderId + " status has been updated to: " + status;
        };
    }
    
    private String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 4) {
            return "****";
        }
        return phoneNumber.substring(0, 2) + "****" + phoneNumber.substring(phoneNumber.length() - 2);
    }
    
    // Broadcast notifications
    public void broadcastSystemNotification(String title, String message) {
        if (!notificationEnabled) return;
        
        try {
            NotificationMessage notification = NotificationMessage.builder()
                .title(title)
                .message(message)
                .type(NotificationType.SYSTEM_ANNOUNCEMENT)
                .createdAt(LocalDateTime.now())
                .priority(NotificationPriority.NORMAL)
                .readStatus(false)
                .build();
                
            messagingTemplate.convertAndSend("/topic/system-notifications", notification);
            log.info("System broadcast sent: {}", title);
        } catch (Exception e) {
            log.error("Failed to send system broadcast: {}", e.getMessage());
        }
    }
    
    // Configuration methods
    public void enableNotifications() {
        this.notificationEnabled = true;
        log.info("Notifications enabled");
    }
    
    public void disableNotifications() {
        this.notificationEnabled = false;
        log.info("Notifications disabled");
    }
    
    public boolean isNotificationEnabled() {
        return notificationEnabled;
    }
}