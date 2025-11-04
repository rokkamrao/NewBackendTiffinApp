package com.tiffin.api.notification.service;

import com.tiffin.api.notification.NotificationSender;
import com.tiffin.api.notification.dto.NotificationDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DefaultNotificationSender implements NotificationSender {
    @Override
    public void sendPushNotification(String fcmToken, NotificationDto dto) {
        // No-op implementation for development/testing. Replace with FCM integration.
        if (fcmToken == null || fcmToken.isBlank()) {
            log.debug("Skipping push notification: no FCM token. type={}, userId={}", dto.getType(), dto.getUserId());
            return;
        }
        log.info("[Push] token={} title='{}' message='{}'", maskToken(fcmToken), dto.getTitle(), dto.getMessage());
    }

    @Override
    public void sendEmailNotification(String email, NotificationDto dto) {
        // No-op implementation for development/testing. Replace with email provider integration.
        if (email == null || email.isBlank()) {
            log.debug("Skipping email notification: no email. type={}, userId={}", dto.getType(), dto.getUserId());
            return;
        }
        log.info("[Email] to={} title='{}' message='{}'", email, dto.getTitle(), dto.getMessage());
    }

    private String maskToken(String token) {
        if (token == null || token.length() < 8) return "****";
        return token.substring(0, 4) + "****" + token.substring(token.length() - 4);
        
    }
}
