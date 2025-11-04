package com.tiffin.api.notification.service;

import com.tiffin.api.notification.dto.NotificationDto;
import com.tiffin.api.notification.model.Notification;
import com.tiffin.api.notification.repository.NotificationRepository;
import com.tiffin.api.payment.model.Payment;
import com.tiffin.api.subscription.model.Subscription;
import com.tiffin.api.user.model.User;
import com.tiffin.api.user.repository.UserRepository;
import com.tiffin.api.notification.NotificationSender;
import com.tiffin.api.notification.dto.NotificationData;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final NotificationSender notificationSender;

    @Async
    @Transactional
        public void sendPaymentSuccessNotification(String userId, Payment payment) {
                Long userIdLong = Long.parseLong(userId);
                User user = userRepository.findById(userIdLong)
                .orElseThrow(() -> new RuntimeException("User not found"));

        NotificationDto notification = NotificationDto.builder()
                .userId(userId)
                .type("PAYMENT_SUCCESS")
                .title("Payment Successful")
                .message("Your payment of " + payment.getAmount() + " " + payment.getCurrency() + " was successful")
                .deepLink("/orders/" + payment.getOrderId())
                .createdAt(LocalDateTime.now())
                .read(false)
                .status("SENT")
                .data(NotificationData.builder()
                                .dataOrderId(payment.getOrderId())
                                .dataPaymentId(payment.getId())
                                .dataStatus("SUCCESS")
                        .build())
                .build();

                saveAndSendNotification(notification, user);
    }

    @Async
    @Transactional
        public void sendPaymentFailureNotification(String userId, Payment payment) {
                Long userIdLong = Long.parseLong(userId);
                User user = userRepository.findById(userIdLong)
                .orElseThrow(() -> new RuntimeException("User not found"));

        NotificationDto notification = NotificationDto.builder()
                .userId(userId)
                .type("PAYMENT_FAILURE")
                .title("Payment Failed")
                .message("Your payment could not be processed. " + payment.getFailureReason())
                .deepLink("/payments/retry/" + payment.getId())
                .createdAt(LocalDateTime.now())
                .read(false)
                .status("SENT")
                .data(NotificationData.builder()
                                .dataOrderId(payment.getOrderId())
                                .dataPaymentId(payment.getId())
                                .dataStatus("FAILED")
                                .dataAdditionalInfo(payment.getFailureReason())
                        .build())
                .build();

                saveAndSendNotification(notification, user);
    }

    @Async
    @Transactional
        public void sendSubscriptionRenewalReminder(Subscription subscription) {
                User user = userRepository.findById(subscription.getUser().getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        NotificationDto notification = NotificationDto.builder()
                .userId(String.valueOf(user.getId()))
                .type("SUBSCRIPTION_RENEWAL")
                .title("Subscription Renewal Reminder")
                .message("Your " + subscription.getPlanType() + " subscription will renew in 2 days")
                .deepLink("/subscriptions/" + subscription.getId())
                .createdAt(LocalDateTime.now())
                .read(false)
                .status("SENT")
                .data(NotificationData.builder()
                                .dataSubscriptionId(String.valueOf(subscription.getId()))
                                .dataStatus("RENEWAL_DUE")
                        .build())
                .build();

        saveAndSendNotification(notification, user);
    }

    @Async
    @Transactional
        public void sendOrderStatusUpdate(String userId, String orderId, String status, String message) {
                Long userIdLong = Long.parseLong(userId);
                User user = userRepository.findById(userIdLong)
                .orElseThrow(() -> new RuntimeException("User not found"));

        NotificationDto notification = NotificationDto.builder()
                .userId(userId)
                .type("ORDER_UPDATE")
                .title("Order Status Update")
                .message(message)
                .deepLink("/orders/" + orderId)
                .createdAt(LocalDateTime.now())
                .read(false)
                .status("SENT")
                .data(NotificationData.builder()
                                .dataOrderId(orderId)
                                .dataStatus(status)
                        .build())
                .build();

        saveAndSendNotification(notification, user);
    }

    @Transactional(readOnly = true)
    public Page<NotificationDto> getUserNotifications(String userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::mapToDto);
    }

    @Transactional
    public void markAsRead(String notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(String userId) {
        notificationRepository.markAllAsRead(userId);
    }

    private void saveAndSendNotification(NotificationDto notificationDto, User user) {
        // Save to database
        notificationRepository.save(mapToEntity(notificationDto));
        
        // Send push notification if enabled
        if (user.isNotificationsEnabled()) {
            notificationSender.sendPushNotification(user.getFcmToken(), notificationDto);
        }
        
        // Send email notification if configured
        if (user.getEmail() != null && user.isEmailNotificationsEnabled()) {
            notificationSender.sendEmailNotification(user.getEmail(), notificationDto);
        }
    }

    private NotificationDto mapToDto(Notification notification) {
        return NotificationDto.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .deepLink(notification.getDeepLink())
                .createdAt(notification.getCreatedAt())
                .read(notification.isRead())
                .status(notification.getStatus())
                .data(notification.getData())
                .build();
    }

    private Notification mapToEntity(NotificationDto dto) {
        return Notification.builder()
                .userId(dto.getUserId())
                .type(dto.getType())
                .title(dto.getTitle())
                .message(dto.getMessage())
                .deepLink(dto.getDeepLink())
                .createdAt(dto.getCreatedAt())
                .read(dto.isRead())
                .status(dto.getStatus())
                .data(dto.getData())
                .build();
    }
}