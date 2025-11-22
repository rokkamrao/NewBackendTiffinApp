package com.example.tiffinapi.audit.service;

import com.example.tiffinapi.audit.model.AuditLog;
import com.example.tiffinapi.audit.repository.AuditLogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Service for handling audit logging
 */
@Service
public class AuditService {

    private static final Logger logger = LoggerFactory.getLogger(AuditService.class);

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public AuditService(AuditLogRepository auditLogRepository, ObjectMapper objectMapper) {
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = objectMapper;
    }

    @Async("taskExecutor")
    public void logAction(String entityType, Long entityId, AuditLog.AuditAction action, 
                         Long userId, String userEmail, Object oldValues, Object newValues, String details) {
        try {
            HttpServletRequest request = getCurrentRequest();
            
            AuditLog auditLog = AuditLog.builder()
                    .entityType(entityType)
                    .entityId(entityId)
                    .action(action)
                    .userId(userId)
                    .userEmail(userEmail)
                    .oldValues(serializeObject(oldValues))
                    .newValues(serializeObject(newValues))
                    .details(details)
                    .ipAddress(getClientIpAddress(request))
                    .userAgent(getUserAgent(request))
                    .sessionId(getSessionId(request))
                    .source("API")
                    .build();

            if (auditLog != null) {
                auditLogRepository.save(auditLog);
                logger.info("Audit log created: {} {} for entity: {} id: {}", 
                        action, entityType, entityType, entityId);
            }
        } catch (Exception e) {
            logger.error("Failed to create audit log", e);
        }
    }

    public void logUserAction(Long userId, String userEmail, AuditLog.AuditAction action, String details) {
        logAction("User", userId, action, userId, userEmail, null, null, details);
    }

    public void logEntityCreation(String entityType, Long entityId, Long userId, String userEmail, Object entity) {
        logAction(entityType, entityId, AuditLog.AuditAction.CREATE, userId, userEmail, null, entity, "Entity created");
    }

    public void logEntityUpdate(String entityType, Long entityId, Long userId, String userEmail, 
                               Object oldEntity, Object newEntity) {
        logAction(entityType, entityId, AuditLog.AuditAction.UPDATE, userId, userEmail, oldEntity, newEntity, "Entity updated");
    }

    public void logEntityDeletion(String entityType, Long entityId, Long userId, String userEmail, Object entity) {
        logAction(entityType, entityId, AuditLog.AuditAction.DELETE, userId, userEmail, entity, null, "Entity deleted");
    }

    public void logLogin(Long userId, String userEmail) {
        logUserAction(userId, userEmail, AuditLog.AuditAction.LOGIN, "User logged in");
    }

    public void logLogout(Long userId, String userEmail) {
        logUserAction(userId, userEmail, AuditLog.AuditAction.LOGOUT, "User logged out");
    }

    public void logPasswordChange(Long userId, String userEmail) {
        logUserAction(userId, userEmail, AuditLog.AuditAction.PASSWORD_CHANGE, "Password changed");
    }

    public void logEmailVerification(Long userId, String userEmail) {
        logUserAction(userId, userEmail, AuditLog.AuditAction.EMAIL_VERIFICATION, "Email verified");
    }

    public void logPhoneVerification(Long userId, String userEmail) {
        logUserAction(userId, userEmail, AuditLog.AuditAction.PHONE_VERIFICATION, "Phone verified");
    }

    public void logOrderPlaced(Long orderId, Long userId, String userEmail, Object order) {
        logAction("Order", orderId, AuditLog.AuditAction.ORDER_PLACED, userId, userEmail, null, order, "Order placed");
    }

    public void logPaymentProcessed(Long paymentId, Long userId, String userEmail, Object payment) {
        logAction("Payment", paymentId, AuditLog.AuditAction.PAYMENT_PROCESSED, userId, userEmail, null, payment, "Payment processed");
    }

    public void logAdminAccess(Long userId, String userEmail, String resource) {
        logUserAction(userId, userEmail, AuditLog.AuditAction.ADMIN_ACCESS, "Admin accessed: " + resource);
    }

    private String serializeObject(Object object) {
        if (object == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            logger.warn("Failed to serialize object for audit log", e);
            return object.toString();
        }
    }

    private HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    private String getClientIpAddress(HttpServletRequest request) {
        if (request == null) return null;
        
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }

    private String getUserAgent(HttpServletRequest request) {
        return request != null ? request.getHeader("User-Agent") : null;
    }

    private String getSessionId(HttpServletRequest request) {
        if (request == null || request.getSession(false) == null) {
            return null;
        }
        return request.getSession(false).getId();
    }
}