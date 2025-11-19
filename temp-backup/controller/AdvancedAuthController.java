package com.tiffin.auth.controller;

import com.tiffin.auth.dto.SessionInfo;
import com.tiffin.auth.service.AdvancedSessionManager;
import com.tiffin.cache.AdvancedCacheManager;
import com.tiffin.cache.CacheStats;
import com.tiffin.common.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import jakarta.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Advanced Authentication Controller
 * Implements high-performance login system similar to Gmail/Instagram
 * Features: Multi-level caching, intelligent session management, security monitoring
 */
@RestController
@RequestMapping("/auth/advanced")
@CrossOrigin(origins = "*")
@Slf4j
public class AdvancedAuthController {
    
    @Autowired
    private AdvancedCacheManager cacheManager;
    
    @Autowired
    private AdvancedSessionManager sessionManager;
    
    // Rate limiting for login attempts
    private final ConcurrentHashMap<String, AtomicInteger> loginAttempts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> loginCooldown = new ConcurrentHashMap<>();
    
    // Device fingerprinting cache
    private final ConcurrentHashMap<String, DeviceTrustLevel> deviceTrustCache = new ConcurrentHashMap<>();
    
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final long COOLDOWN_PERIOD = 15 * 60 * 1000; // 15 minutes
    
    /**
     * Advanced login with intelligent caching and session management
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> advancedLogin(
            @RequestBody AdvancedLoginRequest request,
            HttpServletRequest httpRequest) {
        
        String clientIp = getClientIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        String deviceFingerprint = request.getDeviceFingerprint();
        
        try {
            // 1. Rate limiting check
            if (isRateLimited(clientIp)) {
                return ResponseEntity.status(429)
                    .body(ApiResponse.error("Too many login attempts. Please try again later."));
            }
            
            // 2. Device trust level assessment
            DeviceTrustLevel trustLevel = assessDeviceTrust(deviceFingerprint, clientIp, userAgent);
            
            // 3. Check cache for user credentials (if enabled for this trust level)
            Optional<UserCredentials> cachedUser = Optional.empty();
            if (trustLevel == DeviceTrustLevel.TRUSTED) {
                cachedUser = cacheManager.get("user_creds:" + request.getEmail(), UserCredentials.class);
            }
            
            // 4. Authenticate user
            AuthResult authResult = authenticateUser(request, cachedUser);
            
            if (!authResult.isSuccess()) {
                recordFailedAttempt(clientIp);
                return ResponseEntity.status(401)
                    .body(ApiResponse.error("Invalid credentials"));
            }
            
            // 5. Create advanced session
            String sessionId = sessionManager.createSession(
                authResult.getUserId(),
                authResult.getEmail(),
                authResult.getRole(),
                deviceFingerprint,
                clientIp,
                userAgent
            );
            
            // 6. Cache user data at appropriate level
            cacheUserData(authResult, trustLevel);
            
            // 7. Update device trust
            updateDeviceTrust(deviceFingerprint, clientIp, true);
            
            // 8. Reset rate limiting on successful login
            resetRateLimit(clientIp);
            
            // 9. Generate response
            LoginResponse response = LoginResponse.builder()
                .sessionId(sessionId)
                .userId(authResult.getUserId())
                .email(authResult.getEmail())
                .role(authResult.getRole())
                .trustLevel(trustLevel.name())
                .requiresMFA(trustLevel == DeviceTrustLevel.UNTRUSTED)
                .sessionExpiresAt(System.currentTimeMillis() + 24 * 60 * 60 * 1000)
                .build();
            
            log.info("Successful advanced login for user: {} from IP: {} with trust level: {}", 
                    authResult.getEmail(), clientIp, trustLevel);
            
            return ResponseEntity.ok(ApiResponse.success(response));
            
        } catch (Exception e) {
            log.error("Error during advanced login", e);
            recordFailedAttempt(clientIp);
            return ResponseEntity.status(500)
                .body(ApiResponse.error("Login failed due to internal error"));
        }
    }
    
    /**
     * Session validation with caching
     */
    @PostMapping("/validate-session")
    public ResponseEntity<ApiResponse<SessionValidationResponse>> validateSession(
            @RequestBody SessionValidationRequest request,
            HttpServletRequest httpRequest) {
        
        String clientIp = getClientIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        
        Optional<SessionInfo> session = sessionManager.validateSession(
            request.getSessionId(), clientIp, userAgent);
        
        if (session.isEmpty()) {
            return ResponseEntity.status(401)
                .body(ApiResponse.error("Invalid or expired session"));
        }
        
        SessionValidationResponse response = SessionValidationResponse.builder()
            .valid(true)
            .userId(session.get().getUserId())
            .email(session.get().getUsername())
            .role(session.get().getRole())
            .sessionAge(session.get().getSessionAge())
            .lastActivity(session.get().getLastAccessTime())
            .build();
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    /**
     * Advanced logout with cache cleanup
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> advancedLogout(
            @RequestBody LogoutRequest request,
            HttpServletRequest httpRequest) {
        
        // Invalidate session
        sessionManager.invalidateSession(request.getSessionId());
        
        // Clear related cache entries
        clearUserCacheEntries(request.getSessionId());
        
        log.info("Advanced logout completed for session: {}", request.getSessionId());
        
        return ResponseEntity.ok(ApiResponse.success("Logout successful"));
    }
    
    /**
     * Get cache statistics (admin only)
     */
    @GetMapping("/cache-stats")
    public ResponseEntity<ApiResponse<CacheStats>> getCacheStats() {
        CacheStats stats = cacheManager.getStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
    
    /**
     * Get session statistics (admin only)
     */
    @GetMapping("/session-stats")
    public ResponseEntity<ApiResponse<AdvancedSessionManager.SessionStats>> getSessionStats() {
        AdvancedSessionManager.SessionStats stats = sessionManager.getSessionStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
    
    /**
     * Clear all caches (admin only)
     */
    @PostMapping("/clear-cache")
    public ResponseEntity<ApiResponse<String>> clearCache() {
        cacheManager.clearAll();
        return ResponseEntity.ok(ApiResponse.success("Cache cleared successfully"));
    }
    
    /**
     * Private helper methods
     */
    
    private boolean isRateLimited(String clientIp) {
        AtomicInteger attempts = loginAttempts.get(clientIp);
        Long cooldownEnd = loginCooldown.get(clientIp);
        
        if (cooldownEnd != null && System.currentTimeMillis() < cooldownEnd) {
            return true;
        }
        
        return attempts != null && attempts.get() >= MAX_LOGIN_ATTEMPTS;
    }
    
    private void recordFailedAttempt(String clientIp) {
        AtomicInteger attempts = loginAttempts.computeIfAbsent(clientIp, k -> new AtomicInteger(0));
        int currentAttempts = attempts.incrementAndGet();
        
        if (currentAttempts >= MAX_LOGIN_ATTEMPTS) {
            loginCooldown.put(clientIp, System.currentTimeMillis() + COOLDOWN_PERIOD);
            log.warn("IP {} has been rate limited due to {} failed login attempts", clientIp, currentAttempts);
        }
    }
    
    private void resetRateLimit(String clientIp) {
        loginAttempts.remove(clientIp);
        loginCooldown.remove(clientIp);
    }
    
    private DeviceTrustLevel assessDeviceTrust(String deviceFingerprint, String clientIp, String userAgent) {
        DeviceTrustLevel cached = deviceTrustCache.get(deviceFingerprint);
        if (cached != null) {
            return cached;
        }
        
        // Assess based on various factors
        // This would typically involve ML algorithms
        
        // Simple heuristic for demo
        if (deviceFingerprint != null && deviceFingerprint.length() > 10) {
            deviceTrustCache.put(deviceFingerprint, DeviceTrustLevel.TRUSTED);
            return DeviceTrustLevel.TRUSTED;
        }
        
        deviceTrustCache.put(deviceFingerprint, DeviceTrustLevel.UNTRUSTED);
        return DeviceTrustLevel.UNTRUSTED;
    }
    
    private void updateDeviceTrust(String deviceFingerprint, String clientIp, boolean successful) {
        if (successful) {
            deviceTrustCache.put(deviceFingerprint, DeviceTrustLevel.TRUSTED);
        }
    }
    
    private AuthResult authenticateUser(AdvancedLoginRequest request, Optional<UserCredentials> cachedUser) {
        // This would typically integrate with your user service
        // For demo purposes, using simplified logic
        
        if (cachedUser.isPresent()) {
            UserCredentials user = cachedUser.get();
            if (user.getEmail().equals(request.getEmail()) && 
                validatePassword(request.getPassword(), user.getPasswordHash())) {
                
                return AuthResult.builder()
                    .success(true)
                    .userId(user.getUserId())
                    .email(user.getEmail())
                    .role(user.getRole())
                    .build();
            }
        }
        
        // Fallback to database lookup
        // This would be your actual authentication logic
        if ("admin@tiffin.com".equals(request.getEmail()) && "admin@123".equals(request.getPassword())) {
            return AuthResult.builder()
                .success(true)
                .userId("1")
                .email("admin@tiffin.com")
                .role("ADMIN")
                .build();
        }
        
        return AuthResult.builder().success(false).build();
    }
    
    private boolean validatePassword(String plainPassword, String hashedPassword) {
        // This would use proper password hashing like BCrypt
        return plainPassword.equals(hashedPassword); // Simplified for demo
    }
    
    private void cacheUserData(AuthResult authResult, DeviceTrustLevel trustLevel) {
        if (trustLevel == DeviceTrustLevel.TRUSTED) {
            UserCredentials userCreds = UserCredentials.builder()
                .userId(authResult.getUserId())
                .email(authResult.getEmail())
                .role(authResult.getRole())
                .passwordHash(authResult.getPassword()) // This would be hashed
                .build();
            
            cacheManager.put("user_creds:" + authResult.getEmail(), userCreds);
        }
        
        // Cache user profile data
        UserProfile profile = UserProfile.builder()
            .userId(authResult.getUserId())
            .email(authResult.getEmail())
            .role(authResult.getRole())
            .build();
        
        cacheManager.put("user_profile:" + authResult.getUserId(), profile);
    }
    
    private void clearUserCacheEntries(String sessionId) {
        // Get session info to find user
        Optional<SessionInfo> session = cacheManager.get("session:" + sessionId, SessionInfo.class);
        if (session.isPresent()) {
            String userId = session.get().getUserId();
            String email = session.get().getUsername();
            
            // Clear user-specific cache entries
            cacheManager.put("user_creds:" + email, null);
            cacheManager.put("user_profile:" + userId, null);
            cacheManager.put("user_sessions:" + userId, null);
        }
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
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
    
    /**
     * Enums and DTOs
     */
    
    public enum DeviceTrustLevel {
        TRUSTED, SUSPICIOUS, UNTRUSTED
    }
    
    @lombok.Data
    @lombok.Builder
    public static class AdvancedLoginRequest {
        private String email;
        private String password;
        private String deviceFingerprint;
        private boolean rememberDevice;
        private String mfaCode;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class LoginResponse {
        private String sessionId;
        private String userId;
        private String email;
        private String role;
        private String trustLevel;
        private boolean requiresMFA;
        private long sessionExpiresAt;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class SessionValidationRequest {
        private String sessionId;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class SessionValidationResponse {
        private boolean valid;
        private String userId;
        private String email;
        private String role;
        private long sessionAge;
        private long lastActivity;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class LogoutRequest {
        private String sessionId;
        private boolean logoutAllDevices;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class AuthResult {
        private boolean success;
        private String userId;
        private String email;
        private String role;
        private String password;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class UserCredentials {
        private String userId;
        private String email;
        private String passwordHash;
        private String role;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class UserProfile {
        private String userId;
        private String email;
        private String role;
        private String name;
        private String avatar;
    }
}