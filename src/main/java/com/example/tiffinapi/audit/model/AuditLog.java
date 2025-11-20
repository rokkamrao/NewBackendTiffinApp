package com.example.tiffinapi.audit.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entity for storing audit logs
 */
@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_entity", columnList = "entityType, entityId"),
    @Index(name = "idx_audit_user", columnList = "userId"),
    @Index(name = "idx_audit_timestamp", columnList = "timestamp"),
    @Index(name = "idx_audit_action", columnList = "action")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String entityType; // User, Order, Payment, etc.

    @Column(nullable = false)
    private Long entityId;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private AuditAction action;

    @Column
    private Long userId; // Who performed the action

    @Column(length = 100)
    private String userEmail;

    @Column(length = 45)
    private String ipAddress;

    @Column(length = 500)
    private String userAgent;

    @Column(columnDefinition = "TEXT")
    private String oldValues; // JSON representation of old values

    @Column(columnDefinition = "TEXT")
    private String newValues; // JSON representation of new values

    @Column(length = 500)
    private String details; // Additional context

    @CreationTimestamp
    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(length = 100)
    private String sessionId;

    @Column(length = 50)
    private String source; // WEB, API, MOBILE, ADMIN

    public enum AuditAction {
        CREATE,
        UPDATE,
        DELETE,
        LOGIN,
        LOGOUT,
        PASSWORD_CHANGE,
        EMAIL_VERIFICATION,
        PHONE_VERIFICATION,
        ORDER_PLACED,
        ORDER_UPDATED,
        PAYMENT_PROCESSED,
        REFUND_INITIATED,
        ADMIN_ACCESS
    }
}