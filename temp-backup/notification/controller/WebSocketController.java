package com.tiffin.notification.controller;

import com.tiffin.notification.service.RealTimeNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * WebSocket controller for handling real-time communication
 */
@Controller
@Slf4j
public class WebSocketController {
    
    @Autowired
    private RealTimeNotificationService notificationService;
    
    /**
     * Handle client connection and send welcome message
     */
    @MessageMapping("/connect")
    @SendToUser("/queue/connected")
    public Map<String, Object> handleConnect(Authentication authentication) {
        try {
            String username = authentication != null ? authentication.getName() : "Anonymous";
            log.info("WebSocket connection established for user: {}", username);
            
            return Map.of(
                "type", "CONNECTION_ESTABLISHED",
                "message", "WebSocket connection established successfully",
                "timestamp", System.currentTimeMillis(),
                "user", username
            );
            
        } catch (Exception e) {
            log.error("Error handling WebSocket connection", e);
            return Map.of(
                "type", "CONNECTION_ERROR",
                "message", "Failed to establish connection",
                "timestamp", System.currentTimeMillis()
            );
        }
    }
    
    /**
     * Handle notification acknowledgment from client
     */
    @MessageMapping("/notification/ack")
    public void handleNotificationAck(@Payload Map<String, Object> ackPayload, 
                                    Authentication authentication) {
        try {
            Long notificationId = Long.valueOf(ackPayload.get("notificationId").toString());
            String username = authentication != null ? authentication.getName() : "Anonymous";
            
            log.debug("Notification {} acknowledged by user: {}", notificationId, username);
            
            // Here you could mark the notification as acknowledged in the database
            // or perform any other necessary actions
            
        } catch (Exception e) {
            log.error("Error handling notification acknowledgment", e);
        }
    }
    
    /**
     * Handle typing indicator for chat messages
     */
    @MessageMapping("/chat/typing")
    @SendToUser("/queue/chat/typing")
    public Map<String, Object> handleTypingIndicator(@Payload Map<String, Object> typingData,
                                                    Authentication authentication) {
        try {
            String username = authentication != null ? authentication.getName() : "Anonymous";
            Boolean isTyping = (Boolean) typingData.get("isTyping");
            
            return Map.of(
                "type", "TYPING_INDICATOR",
                "user", username,
                "isTyping", isTyping,
                "timestamp", System.currentTimeMillis()
            );
            
        } catch (Exception e) {
            log.error("Error handling typing indicator", e);
            return Map.of(
                "type", "ERROR",
                "message", "Failed to process typing indicator"
            );
        }
    }
    
    /**
     * Handle delivery partner location updates
     */
    @MessageMapping("/delivery/location")
    public void handleLocationUpdate(@Payload Map<String, Object> locationData,
                                   Authentication authentication) {
        try {
            String username = authentication != null ? authentication.getName() : "Anonymous";
            Long deliveryId = Long.valueOf(locationData.get("deliveryId").toString());
            Double latitude = Double.valueOf(locationData.get("latitude").toString());
            Double longitude = Double.valueOf(locationData.get("longitude").toString());
            
            log.debug("Location update received from delivery partner {}: lat={}, lng={}", 
                     username, latitude, longitude);
            
            // Process location update and notify customers
            // This would typically involve calling a service to update delivery location
            // and notify relevant customers
            
        } catch (Exception e) {
            log.error("Error handling location update", e);
        }
    }
    
    /**
     * Handle client heartbeat/ping
     */
    @MessageMapping("/ping")
    @SendToUser("/queue/pong")
    public Map<String, Object> handlePing(@Payload Map<String, Object> pingData) {
        return Map.of(
            "type", "PONG",
            "timestamp", System.currentTimeMillis(),
            "clientTimestamp", pingData.get("timestamp")
        );
    }
}