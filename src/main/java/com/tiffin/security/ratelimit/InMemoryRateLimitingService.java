package com.tiffin.security.ratelimit;

import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * In-memory rate limiting service
 * Implements sliding window rate limiting algorithm
 */
@Component
@Slf4j
public class InMemoryRateLimitingService {
    
    // In-memory storage for rate limiting
    private final ConcurrentHashMap<String, RateLimitEntry> rateLimitStore = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, LocalDateTime> blockedIps = new ConcurrentHashMap<>();
    
    // Rate limiting configurations
    private static final int LOGIN_ATTEMPTS_LIMIT = 5;
    private static final int API_REQUESTS_LIMIT = 100;
    private static final int WINDOW_SIZE_MINUTES = 15;
    
    /**
     * Rate limit entry to store count and window start time
     */
    private static class RateLimitEntry {
        private final AtomicInteger count = new AtomicInteger(0);
        private volatile LocalDateTime windowStart;
        private final Duration windowDuration;
        
        public RateLimitEntry(Duration windowDuration) {
            this.windowDuration = windowDuration;
            this.windowStart = LocalDateTime.now();
        }
        
        public boolean isExpired() {
            return LocalDateTime.now().isAfter(windowStart.plus(windowDuration));
        }
        
        public void reset() {
            count.set(0);
            windowStart = LocalDateTime.now();
        }
        
        public int incrementAndGet() {
            return count.incrementAndGet();
        }
        
        public int get() {
            return count.get();
        }
    }
    
    /**
     * Check if the request is within rate limit
     */
    public boolean isAllowed(String key, int limit, Duration window) {
        try {
            RateLimitEntry entry = rateLimitStore.computeIfAbsent(key, k -> new RateLimitEntry(window));
            
            // Check if window has expired
            if (entry.isExpired()) {
                entry.reset();
            }
            
            // Check if current count exceeds limit
            if (entry.get() >= limit) {
                log.warn("Rate limit exceeded for key: {}, count: {}, limit: {}", key, entry.get(), limit);
                return false;
            }
            
            // Increment counter
            entry.incrementAndGet();
            return true;
            
        } catch (Exception e) {
            log.error("Error checking rate limit for key: {}", key, e);
            return true; // Allow request on error
        }
    }
    
    /**
     * Check login rate limit
     */
    public boolean isLoginAllowed(String ipAddress) {
        return isAllowed("login:" + ipAddress, LOGIN_ATTEMPTS_LIMIT, Duration.ofMinutes(WINDOW_SIZE_MINUTES));
    }
    
    /**
     * Check API rate limit
     */
    public boolean isApiCallAllowed(String userId) {
        return isAllowed("api:" + userId, API_REQUESTS_LIMIT, Duration.ofMinutes(1));
    }
    
    /**
     * Check general IP rate limit
     */
    public boolean isIpAllowed(String ipAddress) {
        return isAllowed("ip:" + ipAddress, API_REQUESTS_LIMIT * 2, Duration.ofMinutes(1));
    }
    
    /**
     * Get remaining attempts
     */
    public int getRemainingAttempts(String key, int limit) {
        try {
            RateLimitEntry entry = rateLimitStore.get(key);
            if (entry == null || entry.isExpired()) {
                return limit;
            }
            return Math.max(0, limit - entry.get());
        } catch (Exception e) {
            log.error("Error getting remaining attempts for key: {}", key, e);
            return limit;
        }
    }
    
    /**
     * Reset rate limit for a key (admin function)
     */
    public void resetRateLimit(String key) {
        try {
            rateLimitStore.remove(key);
            log.info("Rate limit reset for key: {}", key);
        } catch (Exception e) {
            log.error("Error resetting rate limit for key: {}", key, e);
        }
    }
    
    /**
     * Block IP address temporarily
     */
    public void blockIp(String ipAddress, Duration duration) {
        try {
            LocalDateTime unblockTime = LocalDateTime.now().plus(duration);
            blockedIps.put(ipAddress, unblockTime);
            log.warn("IP address blocked: {} until: {}", ipAddress, unblockTime);
        } catch (Exception e) {
            log.error("Error blocking IP address: {}", ipAddress, e);
        }
    }
    
    /**
     * Check if IP is blocked
     */
    public boolean isIpBlocked(String ipAddress) {
        try {
            LocalDateTime unblockTime = blockedIps.get(ipAddress);
            if (unblockTime == null) {
                return false;
            }
            
            // Check if block has expired
            if (LocalDateTime.now().isAfter(unblockTime)) {
                blockedIps.remove(ipAddress);
                return false;
            }
            
            return true;
        } catch (Exception e) {
            log.error("Error checking if IP is blocked: {}", ipAddress, e);
            return false;
        }
    }
    
    /**
     * Cleanup expired entries (should be called periodically)
     */
    public void cleanup() {
        try {
            rateLimitStore.entrySet().removeIf(entry -> entry.getValue().isExpired());
            
            LocalDateTime now = LocalDateTime.now();
            blockedIps.entrySet().removeIf(entry -> now.isAfter(entry.getValue()));
            
            log.debug("Rate limit cleanup completed. Active entries: {}, Blocked IPs: {}", 
                    rateLimitStore.size(), blockedIps.size());
        } catch (Exception e) {
            log.error("Error during rate limit cleanup", e);
        }
    }
    
    /**
     * Get rate limiting statistics
     */
    public RateLimitStats getStats() {
        return RateLimitStats.builder()
                .activeEntries(rateLimitStore.size())
                .blockedIps(blockedIps.size())
                .build();
    }
    
    @lombok.Data
    @lombok.Builder
    public static class RateLimitStats {
        private int activeEntries;
        private int blockedIps;
    }
}