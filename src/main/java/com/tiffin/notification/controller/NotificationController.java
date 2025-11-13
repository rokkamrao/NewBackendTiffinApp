package com.tiffin.notification.controller;

import com.tiffin.notification.dto.NotificationMessage;
import com.tiffin.notification.model.Notification;
import com.tiffin.notification.model.NotificationType;
import com.tiffin.notification.model.NotificationPriority;
import com.tiffin.notification.repository.NotificationRepository;
import com.tiffin.notification.service.RealTimeNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * REST controller for notification management
 */
@RestController
@RequestMapping("/notifications")
public class NotificationController {
    
    @Autowired
    private RealTimeNotificationService notificationService;
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    /**
     * Get notifications for current user
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'DELIVERY_PARTNER')")
    public ResponseEntity<Page<Notification>> getNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long userId) {
        
        try {
            // In a real app, you'd get userId from the authenticated user
            if (userId == null) {
                userId = 1L; // Placeholder - get from authentication context
            }
            
            Pageable pageable = PageRequest.of(page, size);
            Page<Notification> notifications = notificationRepository
                    .findByUserIdOrderByCreatedAtDesc(userId, pageable);
            
            return ResponseEntity.ok(notifications);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get unread notifications count
     */
    @GetMapping("/unread-count")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'DELIVERY_PARTNER')")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@RequestParam(required = false) Long userId) {
        try {
            if (userId == null) {
                userId = 1L; // Placeholder
            }
            
            long unreadCount = notificationRepository.countByUserIdAndReadStatusFalse(userId);
            return ResponseEntity.ok(Map.of("unreadCount", unreadCount));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Mark notification as read
     */
    @PutMapping("/{notificationId}/read")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'DELIVERY_PARTNER')")
    public ResponseEntity<Map<String, String>> markAsRead(@PathVariable Long notificationId,
                                                         @RequestParam(required = false) Long userId) {
        try {
            if (userId == null) {
                userId = 1L; // Placeholder
            }
            
            Notification notification = notificationRepository.findById(notificationId)
                    .orElse(null);
            
            if (notification == null || !notification.getUserId().equals(userId)) {
                return ResponseEntity.notFound().build();
            }
            
            notification.markAsRead();
            notificationRepository.save(notification);
            
            return ResponseEntity.ok(Map.of("message", "Notification marked as read"));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to mark notification as read"));
        }
    }
    
    /**
     * Mark multiple notifications as read
     */
    @PutMapping("/read-multiple")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'DELIVERY_PARTNER')")
    public ResponseEntity<Map<String, String>> markMultipleAsRead(
            @RequestBody List<Long> notificationIds,
            @RequestParam(required = false) Long userId) {
        try {
            if (userId == null) {
                userId = 1L; // Placeholder
            }
            
            notificationRepository.markAsReadByIds(notificationIds, userId, LocalDateTime.now());
            
            return ResponseEntity.ok(Map.of(
                "message", "Notifications marked as read",
                "count", String.valueOf(notificationIds.size())
            ));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to mark notifications as read"));
        }
    }
    
    /**
     * Get high priority notifications
     */
    @GetMapping("/high-priority")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'DELIVERY_PARTNER')")
    public ResponseEntity<List<Notification>> getHighPriorityNotifications(
            @RequestParam(required = false) Long userId) {
        try {
            if (userId == null) {
                userId = 1L; // Placeholder
            }
            
            List<Notification> notifications = notificationRepository
                    .findHighPriorityUnreadNotifications(userId);
            
            return ResponseEntity.ok(notifications);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Send test notification (admin only)
     */
    @PostMapping("/test")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> sendTestNotification(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "Test Notification") String title,
            @RequestParam(defaultValue = "This is a test notification") String message) {
        try {
            NotificationMessage testNotification = NotificationMessage.create(
                userId, title, message, NotificationType.SYSTEM_ANNOUNCEMENT, NotificationPriority.NORMAL
            );
            
            notificationService.sendNotificationToUser(testNotification);
            
            return ResponseEntity.ok(Map.of("message", "Test notification sent successfully"));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to send test notification"));
        }
    }
    
    /**
     * Send system announcement (admin only)
     */
    @PostMapping("/announcement")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> sendSystemAnnouncement(
            @RequestParam String title,
            @RequestParam String message,
            @RequestParam(defaultValue = "NORMAL") NotificationPriority priority) {
        try {
            notificationService.sendSystemAnnouncement(title, message, priority);
            
            return ResponseEntity.ok(Map.of("message", "System announcement sent successfully"));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to send system announcement"));
        }
    }
    
    /**
     * Get notification statistics
     */
    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN', 'DELIVERY_PARTNER')")
    public ResponseEntity<NotificationRepository.NotificationStats> getNotificationStats(
            @RequestParam(required = false) Long userId) {
        try {
            if (userId == null) {
                userId = 1L; // Placeholder
            }
            
            NotificationRepository.NotificationStats stats = notificationRepository
                    .getNotificationStats(userId);
            
            return ResponseEntity.ok(stats);
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}