package com.example.tiffinapi.websocket.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Service for handling WebSocket real-time notifications
 */
@Service
public class WebSocketNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketNotificationService.class);

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketNotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Send notification to a specific user
     */
    public void sendToUser(String userId, String destination, Object message) {
        try {
            messagingTemplate.convertAndSendToUser(userId, destination, message);
            logger.info("Sent WebSocket message to user: {} at destination: {}", userId, destination);
        } catch (Exception e) {
            logger.error("Failed to send WebSocket message to user: {}", userId, e);
        }
    }

    /**
     * Send notification to all users subscribed to a topic
     */
    public void sendToTopic(String destination, Object message) {
        try {
            messagingTemplate.convertAndSend("/topic" + destination, message);
            logger.info("Sent WebSocket message to topic: {}", destination);
        } catch (Exception e) {
            logger.error("Failed to send WebSocket message to topic: {}", destination, e);
        }
    }

    /**
     * Send order status update to user
     */
    public void sendOrderUpdate(String userId, Long orderId, String status, String message) {
        OrderUpdateMessage updateMessage = new OrderUpdateMessage(orderId, status, message);
        sendToUser(userId, "/queue/order-updates", updateMessage);
    }

    /**
     * Send delivery tracking update to user
     */
    public void sendDeliveryUpdate(String userId, Long orderId, String status, 
                                  Double latitude, Double longitude, String estimatedTime) {
        DeliveryUpdateMessage updateMessage = new DeliveryUpdateMessage(
                orderId, status, latitude, longitude, estimatedTime);
        sendToUser(userId, "/queue/delivery-updates", updateMessage);
    }

    /**
     * Send payment notification to user
     */
    public void sendPaymentNotification(String userId, String paymentId, String status, String message) {
        PaymentNotificationMessage notification = new PaymentNotificationMessage(paymentId, status, message);
        sendToUser(userId, "/queue/payment-notifications", notification);
    }

    /**
     * Send general notification to user
     */
    public void sendGeneralNotification(String userId, String title, String message, String type) {
        GeneralNotificationMessage notification = new GeneralNotificationMessage(title, message, type);
        sendToUser(userId, "/queue/notifications", notification);
    }

    /**
     * Send system-wide announcement
     */
    public void sendSystemAnnouncement(String title, String message, String type) {
        SystemAnnouncementMessage announcement = new SystemAnnouncementMessage(title, message, type);
        sendToTopic("/system-announcements", announcement);
    }

    // Message classes
    public static class OrderUpdateMessage {
        private final Long orderId;
        private final String status;
        private final String message;
        private final long timestamp;

        public OrderUpdateMessage(Long orderId, String status, String message) {
            this.orderId = orderId;
            this.status = status;
            this.message = message;
            this.timestamp = System.currentTimeMillis();
        }

        public Long getOrderId() { return orderId; }
        public String getStatus() { return status; }
        public String getMessage() { return message; }
        public long getTimestamp() { return timestamp; }
    }

    public static class DeliveryUpdateMessage {
        private final Long orderId;
        private final String status;
        private final Double latitude;
        private final Double longitude;
        private final String estimatedTime;
        private final long timestamp;

        public DeliveryUpdateMessage(Long orderId, String status, Double latitude, 
                                   Double longitude, String estimatedTime) {
            this.orderId = orderId;
            this.status = status;
            this.latitude = latitude;
            this.longitude = longitude;
            this.estimatedTime = estimatedTime;
            this.timestamp = System.currentTimeMillis();
        }

        public Long getOrderId() { return orderId; }
        public String getStatus() { return status; }
        public Double getLatitude() { return latitude; }
        public Double getLongitude() { return longitude; }
        public String getEstimatedTime() { return estimatedTime; }
        public long getTimestamp() { return timestamp; }
    }

    public static class PaymentNotificationMessage {
        private final String paymentId;
        private final String status;
        private final String message;
        private final long timestamp;

        public PaymentNotificationMessage(String paymentId, String status, String message) {
            this.paymentId = paymentId;
            this.status = status;
            this.message = message;
            this.timestamp = System.currentTimeMillis();
        }

        public String getPaymentId() { return paymentId; }
        public String getStatus() { return status; }
        public String getMessage() { return message; }
        public long getTimestamp() { return timestamp; }
    }

    public static class GeneralNotificationMessage {
        private final String title;
        private final String message;
        private final String type;
        private final long timestamp;

        public GeneralNotificationMessage(String title, String message, String type) {
            this.title = title;
            this.message = message;
            this.type = type;
            this.timestamp = System.currentTimeMillis();
        }

        public String getTitle() { return title; }
        public String getMessage() { return message; }
        public String getType() { return type; }
        public long getTimestamp() { return timestamp; }
    }

    public static class SystemAnnouncementMessage {
        private final String title;
        private final String message;
        private final String type;
        private final long timestamp;

        public SystemAnnouncementMessage(String title, String message, String type) {
            this.title = title;
            this.message = message;
            this.type = type;
            this.timestamp = System.currentTimeMillis();
        }

        public String getTitle() { return title; }
        public String getMessage() { return message; }
        public String getType() { return type; }
        public long getTimestamp() { return timestamp; }
    }
}