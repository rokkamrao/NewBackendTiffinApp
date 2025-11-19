package com.tiffin.notification.dto;

import com.tiffin.notification.model.NotificationType;
import com.tiffin.notification.model.NotificationPriority;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO for real-time notification messages
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessage {
    
    private Long id;
    private Long userId;
    private String title;
    private String message;
    private NotificationType type;
    private NotificationPriority priority;
    private Boolean readStatus;
    private Long orderId;
    private Long deliveryId;
    private Map<String, Object> metadata;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    
    /**
     * Static method to create a notification message
     */
    public static NotificationMessage create(Long userId, String title, String message, 
                                           NotificationType type, NotificationPriority priority) {
        return NotificationMessage.builder()
                .userId(userId)
                .title(title)
                .message(message)
                .type(type)
                .priority(priority)
                .readStatus(false)
                .createdAt(LocalDateTime.now())
                .build();
    }
    
    /**
     * Create notification with default title and message from type
     */
    public static NotificationMessage createDefault(Long userId, NotificationType type, 
                                                  NotificationPriority priority) {
        return create(userId, type.getDefaultTitle(), type.getDefaultMessage(), type, priority);
    }
    
    /**
     * Create order-related notification
     */
    public static NotificationMessage createOrderNotification(Long userId, Long orderId, 
                                                            NotificationType type, 
                                                            NotificationPriority priority,
                                                            String customMessage) {
        return NotificationMessage.builder()
                .userId(userId)
                .orderId(orderId)
                .title(type.getDefaultTitle())
                .message(customMessage != null ? customMessage : type.getDefaultMessage())
                .type(type)
                .priority(priority)
                .readStatus(false)
                .createdAt(LocalDateTime.now())
                .build();
    }
    
    /**
     * Create delivery-related notification
     */
    public static NotificationMessage createDeliveryNotification(Long userId, Long deliveryId, 
                                                               NotificationType type, 
                                                               NotificationPriority priority,
                                                               Map<String, Object> locationData) {
        return NotificationMessage.builder()
                .userId(userId)
                .deliveryId(deliveryId)
                .title(type.getDefaultTitle())
                .message(type.getDefaultMessage())
                .type(type)
                .priority(priority)
                .metadata(locationData)
                .readStatus(false)
                .createdAt(LocalDateTime.now())
                .build();
    }
}