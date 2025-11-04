package com.tiffin.api.notification.model;

import com.tiffin.api.notification.dto.NotificationData;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "notifications")
public class Notification {
    @Id
    private String id;

    @Column(nullable = false)
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
