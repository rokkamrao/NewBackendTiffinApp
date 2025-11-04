package com.tiffin.api.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {
    private String id;
    private String userId;
    private String type;
    private String title;
    private String message;
    private String deepLink;
    private LocalDateTime createdAt;
    private boolean read;
    private String status;
    private NotificationData data;
}
// NotificationData is declared in its own file to allow access from other packages