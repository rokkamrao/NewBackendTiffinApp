package com.tiffin.auth.service;

import com.tiffin.cache.AdvancedCacheManager;
import com.tiffin.auth.dto.SessionInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import lombok.extern.slf4j.Slf4j;

import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * Advanced Session Management System
 * Implements sophisticated session handling similar to Gmail, Instagram
 * Features: Multi-device sessions, concurrent session limits, intelligent cleanup
 */
@Service
@Slf4j
public class AdvancedSessionManager {
    
    @Autowired
    private AdvancedCacheManager cacheManager;
    
    // Session storage with different access patterns
    private final ConcurrentHashMap<String, SessionInfo> activeSessions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Set<String>> userSessions = new ConcurrentHashMap<>();
    
    // Concurrent session tracking
    private final ConcurrentHashMap<String, AtomicInteger> userConcurrentSessions = new ConcurrentHashMap<>();
    
    // Session security tracking
    private final ConcurrentHashMap<String, SessionSecurityInfo> sessionSecurity = new ConcurrentHashMap<>();
    
    // Device fingerprinting
    private final ConcurrentHashMap<String, DeviceInfo> deviceRegistry = new ConcurrentHashMap<>();
    
    // Lock for critical operations
    private final ReentrantReadWriteLock sessionLock = new ReentrantReadWriteLock();
    
    // Configuration
    private static final int MAX_SESSIONS_PER_USER = 5;
    private static final long SESSION_TIMEOUT = 24 * 60 * 60 * 1000; // 24 hours
    private static final long IDLE_TIMEOUT = 2 * 60 * 60 * 1000; // 2 hours
    private static final long SECURITY_CHECK_INTERVAL = 60 * 1000; // 1 minute
    
    /**
     * Create new session with advanced tracking
     */
    public String createSession(String userId, String email, String role, String deviceFingerprint, String ipAddress, String userAgent) {
        String sessionId = generateSecureSessionId();
        
        sessionLock.writeLock().lock();
        try {
            // Check concurrent session limit
            enforceSessionLimit(userId);
            
            // Create session info
            SessionInfo sessionInfo = SessionInfo.builder()
                .sessionId(sessionId)
                .userId(userId)
                .username(email)
                .role(role)
                .createdAt(System.currentTimeMillis())
                .lastAccessTime(System.currentTimeMillis())
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .deviceFingerprint(deviceFingerprint)
                .isActive(true)
                .build();
            
            // Store session
            activeSessions.put(sessionId, sessionInfo);
            
            // Track user sessions
            userSessions.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(sessionId);
            
            // Update concurrent session count
            userConcurrentSessions.computeIfAbsent(userId, k -> new AtomicInteger(0)).incrementAndGet();
            
            // Register device
            registerDevice(deviceFingerprint, sessionInfo);
            
            // Create security tracking
            sessionSecurity.put(sessionId, new SessionSecurityInfo(ipAddress, userAgent, deviceFingerprint));
            
            // Cache session for fast access
            cacheManager.put("session:" + sessionId, sessionInfo, SESSION_TIMEOUT);
            cacheManager.put("user_sessions:" + userId, userSessions.get(userId));
            
            log.info("Created session for user: {} with device: {}", email, deviceFingerprint);
            
            return sessionId;
            
        } finally {
            sessionLock.writeLock().unlock();
        }
    }
    
    /**
     * Validate session with security checks
     */
    public Optional<SessionInfo> validateSession(String sessionId, String ipAddress, String userAgent) {
        // Try cache first
        Optional<SessionInfo> cachedSession = cacheManager.get("session:" + sessionId, SessionInfo.class);
        
        SessionInfo session = cachedSession.orElseGet(() -> activeSessions.get(sessionId));
        
        if (session == null || !session.isActive()) {
            return Optional.empty();
        }
        
        // Check expiration
        if (isSessionExpired(session)) {
            invalidateSession(sessionId);
            return Optional.empty();
        }
        
        // Security validation
        if (!validateSessionSecurity(sessionId, ipAddress, userAgent)) {
            log.warn("Security validation failed for session: {}", sessionId);
            // Could invalidate session or require re-authentication
            return Optional.empty();
        }
        
        // Update last access time
        updateSessionAccess(session);
        
        return Optional.of(session);
    }
    
    /**
     * Update session access time and cache
     */
    private void updateSessionAccess(SessionInfo session) {
        session.setLastAccessTime(System.currentTimeMillis());
        activeSessions.put(session.getSessionId(), session);
        
        // Update cache with extended TTL for active sessions
        cacheManager.put("session:" + session.getSessionId(), session, SESSION_TIMEOUT);
    }
    
    /**
     * Invalidate specific session
     */
    public void invalidateSession(String sessionId) {
        sessionLock.writeLock().lock();
        try {
            SessionInfo session = activeSessions.remove(sessionId);
            if (session != null) {
                // Remove from user sessions
                Set<String> userSessionSet = userSessions.get(session.getUserId());
                if (userSessionSet != null) {
                    userSessionSet.remove(sessionId);
                    if (userSessionSet.isEmpty()) {
                        userSessions.remove(session.getUserId());
                    }
                }
                
                // Decrement concurrent session count
                AtomicInteger count = userConcurrentSessions.get(session.getUserId());
                if (count != null) {
                    count.decrementAndGet();
                }
                
                // Remove security tracking
                sessionSecurity.remove(sessionId);
                
                // Remove from cache
                cacheManager.put("session:" + sessionId, null); // Explicit cache invalidation
                
                log.info("Invalidated session: {} for user: {}", sessionId, session.getUsername());
            }
        } finally {
            sessionLock.writeLock().unlock();
        }
    }
    
    /**
     * Invalidate all sessions for a user
     */
    public void invalidateAllUserSessions(String userId) {
        sessionLock.writeLock().lock();
        try {
            Set<String> userSessionSet = userSessions.get(userId);
            if (userSessionSet != null) {
                // Create copy to avoid concurrent modification
                Set<String> sessionsToInvalidate = new HashSet<>(userSessionSet);
                
                for (String sessionId : sessionsToInvalidate) {
                    invalidateSession(sessionId);
                }
                
                userSessions.remove(userId);
                userConcurrentSessions.remove(userId);
                
                log.info("Invalidated all sessions for user: {}", userId);
            }
        } finally {
            sessionLock.writeLock().unlock();
        }
    }
    
    /**
     * Enforce concurrent session limit
     */
    private void enforceSessionLimit(String userId) {
        Set<String> userSessionSet = userSessions.get(userId);
        if (userSessionSet != null && userSessionSet.size() >= MAX_SESSIONS_PER_USER) {
            // Find oldest session to remove
            String oldestSessionId = userSessionSet.stream()
                .map(activeSessions::get)
                .filter(Objects::nonNull)
                .min(Comparator.comparing(SessionInfo::getCreatedAt))
                .map(SessionInfo::getSessionId)
                .orElse(null);
            
            if (oldestSessionId != null) {
                invalidateSession(oldestSessionId);
                log.info("Removed oldest session {} due to limit for user: {}", oldestSessionId, userId);
            }
        }
    }
    
    /**
     * Check if session is expired
     */
    private boolean isSessionExpired(SessionInfo session) {
        long now = System.currentTimeMillis();
        
        // Check absolute timeout
        if (now - session.getCreatedAt() > SESSION_TIMEOUT) {
            return true;
        }
        
        // Check idle timeout
        if (now - session.getLastAccessTime() > IDLE_TIMEOUT) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Validate session security
     */
    private boolean validateSessionSecurity(String sessionId, String currentIp, String currentUserAgent) {
        SessionSecurityInfo securityInfo = sessionSecurity.get(sessionId);
        if (securityInfo == null) {
            return false;
        }
        
        // Check for suspicious IP changes
        if (!securityInfo.getOriginalIp().equals(currentIp)) {
            // Allow IP changes but log for monitoring
            log.info("IP change detected for session {}: {} -> {}", sessionId, securityInfo.getOriginalIp(), currentIp);
            securityInfo.addIpChange(currentIp);
        }
        
        // Check for user agent changes (could indicate session hijacking)
        if (!securityInfo.getOriginalUserAgent().equals(currentUserAgent)) {
            log.warn("User agent change detected for session {}", sessionId);
            // Depending on security policy, could invalidate session
            return false;
        }
        
        return true;
    }
    
    /**
     * Register device information
     */
    private void registerDevice(String deviceFingerprint, SessionInfo sessionInfo) {
        DeviceInfo device = deviceRegistry.computeIfAbsent(deviceFingerprint, 
            k -> new DeviceInfo(deviceFingerprint, sessionInfo.getUserAgent(), sessionInfo.getIpAddress()));
        
        device.updateLastSeen();
        device.addSession(sessionInfo.getSessionId());
    }
    
    /**
     * Generate cryptographically secure session ID
     */
    private String generateSecureSessionId() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        
        return sb.toString();
    }
    
    /**
     * Get user's active sessions
     */
    public List<SessionInfo> getUserActiveSessions(String userId) {
        Set<String> userSessionSet = userSessions.get(userId);
        if (userSessionSet == null) {
            return Collections.emptyList();
        }
        
        return userSessionSet.stream()
            .map(activeSessions::get)
            .filter(Objects::nonNull)
            .filter(session -> !isSessionExpired(session))
            .collect(Collectors.toList());
    }
    
    /**
     * Get session statistics
     */
    public SessionStats getSessionStats() {
        return SessionStats.builder()
            .totalActiveSessions(activeSessions.size())
            .totalActiveUsers(userSessions.size())
            .averageSessionsPerUser(calculateAverageSessionsPerUser())
            .build();
    }
    
    private double calculateAverageSessionsPerUser() {
        if (userSessions.isEmpty()) {
            return 0.0;
        }
        
        int totalSessions = userSessions.values().stream()
            .mapToInt(Set::size)
            .sum();
        
        return (double) totalSessions / userSessions.size();
    }
    
    /**
     * Scheduled cleanup of expired sessions
     */
    @Scheduled(fixedRate = 300000) // Every 5 minutes
    public void cleanupExpiredSessions() {
        log.debug("Starting session cleanup...");
        
        sessionLock.writeLock().lock();
        try {
            List<String> expiredSessions = activeSessions.values().stream()
                .filter(this::isSessionExpired)
                .map(SessionInfo::getSessionId)
                .collect(Collectors.toList());
            
            for (String sessionId : expiredSessions) {
                invalidateSession(sessionId);
            }
            
            log.debug("Cleaned up {} expired sessions", expiredSessions.size());
            
        } finally {
            sessionLock.writeLock().unlock();
        }
    }
    
    /**
     * Scheduled security check
     */
    @Scheduled(fixedRate = SECURITY_CHECK_INTERVAL)
    public void performSecurityCheck() {
        // Check for suspicious patterns
        // This could include ML-based anomaly detection
        
        sessionSecurity.values().forEach(securityInfo -> {
            if (securityInfo.getIpChangeCount() > 5) {
                log.warn("Multiple IP changes detected for session, potential security risk");
                // Could trigger additional security measures
            }
        });
    }
    
    /**
     * Inner classes for session tracking
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    private static class SessionSecurityInfo {
        private String originalIp;
        private String originalUserAgent;
        private String deviceFingerprint;
        private List<String> ipHistory = new ArrayList<>();
        private int ipChangeCount = 0;
        
        public SessionSecurityInfo(String ip, String userAgent, String deviceFingerprint) {
            this.originalIp = ip;
            this.originalUserAgent = userAgent;
            this.deviceFingerprint = deviceFingerprint;
            this.ipHistory.add(ip);
        }
        
        public void addIpChange(String newIp) {
            ipHistory.add(newIp);
            ipChangeCount++;
        }
    }
    
    @lombok.Data
    @lombok.AllArgsConstructor
    private static class DeviceInfo {
        private String fingerprint;
        private String userAgent;
        private String lastKnownIp;
        private long lastSeen;
        private Set<String> sessions = new HashSet<>();
        
        public DeviceInfo(String fingerprint, String userAgent, String ip) {
            this.fingerprint = fingerprint;
            this.userAgent = userAgent;
            this.lastKnownIp = ip;
            this.lastSeen = System.currentTimeMillis();
        }
        
        public void updateLastSeen() {
            this.lastSeen = System.currentTimeMillis();
        }
        
        public void addSession(String sessionId) {
            sessions.add(sessionId);
        }
    }
    
    @lombok.Data
    @lombok.Builder
    public static class SessionStats {
        private int totalActiveSessions;
        private int totalActiveUsers;
        private double averageSessionsPerUser;
    }
}