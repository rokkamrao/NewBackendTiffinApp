package com.tiffin.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Session Information DTO
 * Contains comprehensive session data for advanced session management
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionInfo {
    
    private String sessionId;
    private String userId;
    private String username;
    private String role;
    private long createdAt;
    private long lastAccessTime;
    private String ipAddress;
    private String userAgent;
    private String deviceFingerprint;
    private boolean isActive;
    
    // Security and tracking fields
    private int loginAttempts;
    private long lastLoginAttempt;
    private String lastKnownLocation;
    private boolean isSuspicious;
    private String securityLevel; // LOW, MEDIUM, HIGH
    
    // Device and browser information
    private String browserName;
    private String browserVersion;
    private String operatingSystem;
    private String deviceType; // MOBILE, TABLET, DESKTOP
    
    // Session metadata
    private long totalRequestCount;
    private long lastRequestTime;
    private String sessionType; // WEB, MOBILE_APP, API
    
    /**
     * Check if session is recently active
     */
    public boolean isRecentlyActive() {
        return System.currentTimeMillis() - lastAccessTime < 300000; // 5 minutes
    }
    
    /**
     * Get session age in milliseconds
     */
    public long getSessionAge() {
        return System.currentTimeMillis() - createdAt;
    }
    
    /**
     * Get idle time in milliseconds
     */
    public long getIdleTime() {
        return System.currentTimeMillis() - lastAccessTime;
    }
    
    /**
     * Update last access time
     */
    public void updateLastAccess() {
        this.lastAccessTime = System.currentTimeMillis();
        this.lastRequestTime = this.lastAccessTime;
        this.totalRequestCount++;
    }
    
    /**
     * Mark session as suspicious
     */
    public void markSuspicious(String reason) {
        this.isSuspicious = true;
        this.securityLevel = "HIGH";
    }
    
    /**
     * Check if session requires re-authentication
     */
    public boolean requiresReauth() {
        return isSuspicious || "HIGH".equals(securityLevel);
    }
}