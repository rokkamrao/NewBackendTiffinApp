package com.tiffin.notification.repository;

import com.tiffin.notification.model.Notification;
import com.tiffin.notification.model.NotificationType;
import com.tiffin.notification.model.NotificationPriority;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for Notification entity
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    /**
     * Find notifications by user ID, ordered by creation date
     */
    Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    /**
     * Find unread notifications by user ID
     */
    List<Notification> findByUserIdAndReadStatusFalseOrderByCreatedAtDesc(Long userId);
    
    /**
     * Count unread notifications for a user
     */
    long countByUserIdAndReadStatusFalse(Long userId);
    
    /**
     * Find notifications by type and user
     */
    List<Notification> findByUserIdAndTypeOrderByCreatedAtDesc(Long userId, NotificationType type);
    
    /**
     * Find notifications by priority and user
     */
    List<Notification> findByUserIdAndPriorityOrderByCreatedAtDesc(Long userId, NotificationPriority priority);
    
    /**
     * Find notifications by order ID
     */
    List<Notification> findByOrderIdOrderByCreatedAtDesc(Long orderId);
    
    /**
     * Find notifications by delivery ID
     */
    List<Notification> findByDeliveryIdOrderByCreatedAtDesc(Long deliveryId);
    
    /**
     * Find expired notifications
     */
    @Query("SELECT n FROM Notification n WHERE n.expiresAt IS NOT NULL AND n.expiresAt < :currentTime")
    List<Notification> findExpiredNotifications(@Param("currentTime") LocalDateTime currentTime);
    
    /**
     * Find notifications created within a time range
     */
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.createdAt BETWEEN :startTime AND :endTime ORDER BY n.createdAt DESC")
    List<Notification> findByUserIdAndCreatedAtBetween(@Param("userId") Long userId, 
                                                       @Param("startTime") LocalDateTime startTime, 
                                                       @Param("endTime") LocalDateTime endTime);
    
    /**
     * Find high priority unread notifications
     */
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.readStatus = false AND n.priority IN ('HIGH', 'URGENT') ORDER BY n.priority DESC, n.createdAt DESC")
    List<Notification> findHighPriorityUnreadNotifications(@Param("userId") Long userId);
    
    /**
     * Mark notifications as read by IDs
     */
    @Query("UPDATE Notification n SET n.readStatus = true, n.readAt = :readTime WHERE n.id IN :ids AND n.userId = :userId")
    void markAsReadByIds(@Param("ids") List<Long> ids, @Param("userId") Long userId, @Param("readTime") LocalDateTime readTime);
    
    /**
     * Delete old read notifications (cleanup)
     */
    @Query("DELETE FROM Notification n WHERE n.readStatus = true AND n.createdAt < :cutoffDate")
    void deleteOldReadNotifications(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Get notification statistics for a user
     */
    @Query("SELECT " +
           "COUNT(n) as totalCount, " +
           "SUM(CASE WHEN n.readStatus = false THEN 1 ELSE 0 END) as unreadCount, " +
           "SUM(CASE WHEN n.priority = 'HIGH' OR n.priority = 'URGENT' THEN 1 ELSE 0 END) as highPriorityCount " +
           "FROM Notification n WHERE n.userId = :userId")
    NotificationStats getNotificationStats(@Param("userId") Long userId);
    
    /**
     * Interface for notification statistics projection
     */
    interface NotificationStats {
        Long getTotalCount();
        Long getUnreadCount();
        Long getHighPriorityCount();
    }
}