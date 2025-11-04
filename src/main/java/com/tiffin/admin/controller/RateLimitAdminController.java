package com.tiffin.admin.controller;

import com.tiffin.security.ratelimit.InMemoryRateLimitingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

/**
 * Admin controller for rate limiting management
 */
@RestController
@RequestMapping("/api/admin/rate-limit")
@PreAuthorize("hasRole('ADMIN')")
public class RateLimitAdminController {
    
    @Autowired
    private InMemoryRateLimitingService rateLimitService;
    
    /**
     * Get rate limiting statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<InMemoryRateLimitingService.RateLimitStats> getStats() {
        try {
            InMemoryRateLimitingService.RateLimitStats stats = rateLimitService.getStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Reset rate limit for a specific key
     */
    @PostMapping("/reset")
    public ResponseEntity<Map<String, String>> resetRateLimit(@RequestParam String key) {
        try {
            rateLimitService.resetRateLimit(key);
            return ResponseEntity.ok(Map.of("message", "Rate limit reset for key: " + key));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to reset rate limit"));
        }
    }
    
    /**
     * Block IP address
     */
    @PostMapping("/block-ip")
    public ResponseEntity<Map<String, String>> blockIp(
            @RequestParam String ipAddress,
            @RequestParam(defaultValue = "30") int durationMinutes) {
        try {
            rateLimitService.blockIp(ipAddress, Duration.ofMinutes(durationMinutes));
            return ResponseEntity.ok(Map.of(
                "message", "IP blocked: " + ipAddress + " for " + durationMinutes + " minutes"
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to block IP"));
        }
    }
    
    /**
     * Check if IP is blocked
     */
    @GetMapping("/check-ip")
    public ResponseEntity<Map<String, Object>> checkIp(@RequestParam String ipAddress) {
        try {
            boolean isBlocked = rateLimitService.isIpBlocked(ipAddress);
            return ResponseEntity.ok(Map.of(
                "ipAddress", ipAddress,
                "isBlocked", isBlocked
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to check IP status"));
        }
    }
    
    /**
     * Get remaining attempts for a key
     */
    @GetMapping("/remaining")
    public ResponseEntity<Map<String, Object>> getRemainingAttempts(
            @RequestParam String key,
            @RequestParam(defaultValue = "100") int limit) {
        try {
            int remaining = rateLimitService.getRemainingAttempts(key, limit);
            return ResponseEntity.ok(Map.of(
                "key", key,
                "remaining", remaining,
                "limit", limit
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to get remaining attempts"));
        }
    }
    
    /**
     * Trigger manual cleanup
     */
    @PostMapping("/cleanup")
    public ResponseEntity<Map<String, String>> triggerCleanup() {
        try {
            rateLimitService.cleanup();
            return ResponseEntity.ok(Map.of("message", "Cleanup completed successfully"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to perform cleanup"));
        }
    }
}