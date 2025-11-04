package com.tiffin.security.ratelimit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

/**
 * Scheduled tasks for rate limiting maintenance
 */
@Component
@Slf4j
public class RateLimitScheduler {
    
    @Autowired
    private InMemoryRateLimitingService rateLimitService;
    
    /**
     * Cleanup expired rate limit entries every 10 minutes
     */
    @Scheduled(fixedRate = 600000) // 10 minutes in milliseconds
    public void cleanupExpiredEntries() {
        try {
            log.debug("Starting rate limit cleanup...");
            rateLimitService.cleanup();
            log.debug("Rate limit cleanup completed");
        } catch (Exception e) {
            log.error("Error during rate limit cleanup", e);
        }
    }
    
    /**
     * Log rate limiting statistics every hour
     */
    @Scheduled(fixedRate = 3600000) // 1 hour in milliseconds
    public void logStatistics() {
        try {
            InMemoryRateLimitingService.RateLimitStats stats = rateLimitService.getStats();
            log.info("Rate limiting statistics - Active entries: {}, Blocked IPs: {}", 
                    stats.getActiveEntries(), stats.getBlockedIps());
        } catch (Exception e) {
            log.error("Error logging rate limit statistics", e);
        }
    }
}