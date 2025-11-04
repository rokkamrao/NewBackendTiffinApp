package com.tiffin.api.notification;

import com.tiffin.api.notification.dto.NotificationDto;

public interface NotificationSender {
    void sendPushNotification(String fcmToken, NotificationDto dto);
    void sendEmailNotification(String email, NotificationDto dto);
}
