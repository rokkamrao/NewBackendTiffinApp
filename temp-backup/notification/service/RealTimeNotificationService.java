package com.tiffin.notification.service;

import com.tiffin.notification.dto.NotificationMessage;
import com.tiffin.notification.model.Notification;
import com.tiffin.notification.model.NotificationType;
import com.tiffin.notification.model.NotificationPriority;
import com.tiffin.notification.repository.NotificationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Service for managing real-time notifications
 */
@Service
@Slf4j
@Transactional
public class RealTimeNotificationService {
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * Send real-time notification to a specific user
     */
    public CompletableFuture<Void> sendNotificationToUser(NotificationMessage notificationMessage) {
        return CompletableFuture.runAsync(() -> {
            try {
                // Save notification to database
                Notification notification = saveNotificationToDatabase(notificationMessage);
                notificationMessage.setId(notification.getId());
                
                // Send real-time notification via WebSocket
                String destination = "/user/" + notificationMessage.getUserId() + "/queue/notifications";
                messagingTemplate.convertAndSend(destination, notificationMessage);
                
                log.info("Real-time notification sent to user {} with type {}", 
                        notificationMessage.getUserId(), notificationMessage.getType());
                
                // Send badge update for unread count
                sendUnreadCountUpdate(notificationMessage.getUserId());
                
            } catch (Exception e) {
                log.error("Failed to send real-time notification to user {}", 
                         notificationMessage.getUserId(), e);
                throw new RuntimeException("Failed to send notification", e);
            }
        });
    }
    
    /**
     * Send notification to multiple users (broadcast)
     */
    public CompletableFuture<Void> sendNotificationToUsers(List<Long> userIds, NotificationMessage template) {
        return CompletableFuture.runAsync(() -> {
            try {
                for (Long userId : userIds) {
                    NotificationMessage userNotification = NotificationMessage.builder()
                            .userId(userId)
                            .title(template.getTitle())
                            .message(template.getMessage())
                            .type(template.getType())
                            .priority(template.getPriority())
                            .metadata(template.getMetadata())
                            .readStatus(false)
                            .createdAt(LocalDateTime.now())
                            .build();
                    
                    sendNotificationToUser(userNotification).join();
                }
                
                log.info("Broadcast notification sent to {} users with type {}", 
                        userIds.size(), template.getType());
                
            } catch (Exception e) {
                log.error("Failed to send broadcast notification", e);
                throw new RuntimeException("Failed to send broadcast notification", e);
            }
        });
    }
    
    /**
     * Send order status update notification
     */
    public void sendOrderStatusNotification(Long userId, Long orderId, NotificationType orderStatus, 
                                          String customMessage) {
        try {
            NotificationPriority priority = determineOrderNotificationPriority(orderStatus);
            NotificationMessage notification = NotificationMessage.createOrderNotification(
                userId, orderId, orderStatus, priority, customMessage);
            
            sendNotificationToUser(notification);
            
        } catch (Exception e) {
            log.error("Failed to send order status notification for order {} to user {}", 
                     orderId, userId, e);
        }
    }
    
    /**
     * Send delivery location update
     */
    public void sendDeliveryLocationUpdate(Long userId, Long deliveryId, Map<String, Object> locationData) {
        try {
            NotificationMessage notification = NotificationMessage.createDeliveryNotification(
                userId, deliveryId, NotificationType.DELIVERY_LOCATION_UPDATE, 
                NotificationPriority.NORMAL, locationData);
            
            sendNotificationToUser(notification);
            
        } catch (Exception e) {
            log.error("Failed to send delivery location update for delivery {} to user {}", 
                     deliveryId, userId, e);
        }
    }
    
    /**
     * Send promotional notification
     */
    public void sendPromotionalNotification(List<Long> targetUserIds, String title, 
                                          String message, Map<String, Object> promotionData) {
        try {
            NotificationMessage template = NotificationMessage.builder()
                    .title(title)
                    .message(message)
                    .type(NotificationType.PROMOTION)
                    .priority(NotificationPriority.LOW)
                    .metadata(promotionData)
                    .build();
            
            sendNotificationToUsers(targetUserIds, template);
            
        } catch (Exception e) {
            log.error("Failed to send promotional notification", e);
        }
    }
    
    /**
     * Send system announcement
     */
    public void sendSystemAnnouncement(String title, String message, NotificationPriority priority) {
        try {
            // Get all active user IDs (you might want to implement this based on your user service)
            List<Long> allUserIds = getAllActiveUserIds();
            
            NotificationMessage template = NotificationMessage.builder()
                    .title(title)
                    .message(message)
                    .type(NotificationType.SYSTEM_ANNOUNCEMENT)
                    .priority(priority)
                    .build();
            
            sendNotificationToUsers(allUserIds, template);
            
        } catch (Exception e) {
            log.error("Failed to send system announcement", e);
        }
    }
    
    /**
     * Send unread count update to user
     */
    private void sendUnreadCountUpdate(Long userId) {
        try {
            long unreadCount = notificationRepository.countByUserIdAndReadStatusFalse(userId);
            
            Map<String, Object> badgeUpdate = Map.of(
                "type", "BADGE_UPDATE",
                "unreadCount", unreadCount,
                "timestamp", LocalDateTime.now()
            );
            
            String destination = "/user/" + userId + "/queue/badge";
            messagingTemplate.convertAndSend(destination, badgeUpdate);
            
        } catch (Exception e) {
            log.error("Failed to send unread count update to user {}", userId, e);
        }
    }
    
    /**
     * Save notification to database
     */
    private Notification saveNotificationToDatabase(NotificationMessage notificationMessage) {
        try {
            String metadataJson = null;
            if (notificationMessage.getMetadata() != null) {
                metadataJson = objectMapper.writeValueAsString(notificationMessage.getMetadata());
            }
            
            Notification notification = Notification.builder()
                    .userId(notificationMessage.getUserId())
                    .title(notificationMessage.getTitle())
                    .message(notificationMessage.getMessage())
                    .type(notificationMessage.getType())
                    .priority(notificationMessage.getPriority())
                    .readStatus(false)
                    .orderId(notificationMessage.getOrderId())
                    .deliveryId(notificationMessage.getDeliveryId())
                    .metadata(metadataJson)
                    .expiresAt(notificationMessage.getExpiresAt())
                    .build();
            
            return notificationRepository.save(notification);
            
        } catch (Exception e) {
            log.error("Failed to save notification to database", e);
            throw new RuntimeException("Failed to save notification", e);
        }
    }
    
    /**
     * Determine notification priority based on order status
     */
    private NotificationPriority determineOrderNotificationPriority(NotificationType orderStatus) {
        return switch (orderStatus) {
            case ORDER_CANCELLED, PAYMENT_FAILED -> NotificationPriority.HIGH;
            case ORDER_DELIVERED, PAYMENT_SUCCESS -> NotificationPriority.NORMAL;
            case ORDER_OUT_FOR_DELIVERY -> NotificationPriority.HIGH;
            default -> NotificationPriority.NORMAL;
        };
    }
    
    /**
     * Get all active user IDs (placeholder - implement based on your user service)
     */
    private List<Long> getAllActiveUserIds() {
        // TODO: Implement this method to get all active user IDs
        // This is a placeholder - you should implement this based on your User service
        return List.of(); // Return empty list for now
    }
}