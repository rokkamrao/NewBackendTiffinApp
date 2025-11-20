package com.tiffin.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate Limiting Filter using Token Bucket Algorithm
 * 
 * Implements rate limiting to prevent abuse and ensure fair usage.
 * Different limits are applied based on endpoint types and user authentication status.
 */
@Component
@Slf4j
public class RateLimitingFilter extends OncePerRequestFilter {
    
    @Value("${app.rate-limit.requests-per-minute:60}")
    private int requestsPerMinute;
    
    @Value("${app.rate-limit.burst-capacity:10}")
    private int burstCapacity;
    
    @Value("${app.rate-limit.auth-requests-per-minute:10}")
    private int authRequestsPerMinute;
    
    private final ConcurrentHashMap<String, Bucket> userBuckets = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Bucket> ipBuckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        String clientKey = getClientIdentifier(request);
        String endpoint = request.getRequestURI();
        
        Bucket bucket = getBucketForClient(clientKey, endpoint);
        
        if (bucket.tryConsume(1)) {
            // Add rate limit headers
            addRateLimitHeaders(response, bucket);
            filterChain.doFilter(request, response);
        } else {
            // Rate limit exceeded
            log.warn("🚫 Rate limit exceeded for client: {} on endpoint: {}", clientKey, endpoint);
            sendRateLimitExceededResponse(response);
        }
    }

    /**
     * Get unique identifier for the client (IP address or user ID if authenticated)
     */
    private String getClientIdentifier(HttpServletRequest request) {
        // In production, you might want to use user ID if authenticated
        // For now, use IP address
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        String xRealIp = request.getHeader("X-Real-IP");
        
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        } else if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        } else {
            return request.getRemoteAddr();
        }
    }

    /**
     * Get or create bucket for client based on endpoint type
     */
    private Bucket getBucketForClient(String clientKey, String endpoint) {
        if (isAuthEndpoint(endpoint)) {
            return ipBuckets.computeIfAbsent(clientKey + ":auth", this::createAuthBucket);
        } else {
            return userBuckets.computeIfAbsent(clientKey, this::createGeneralBucket);
        }
    }

    /**
     * Check if the endpoint is an authentication endpoint (stricter limits)
     */
    private boolean isAuthEndpoint(String endpoint) {
        return endpoint.contains("/auth/") || 
               endpoint.contains("/login") || 
               endpoint.contains("/register") ||
               endpoint.contains("/otp");
    }

    /**
     * Create bucket for general API endpoints
     */
    private Bucket createGeneralBucket(String key) {
        Bandwidth limit = Bandwidth.classic(requestsPerMinute, Refill.intervally(requestsPerMinute, Duration.ofMinutes(1)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Create bucket for authentication endpoints (stricter limits)
     */
    private Bucket createAuthBucket(String key) {
        Bandwidth limit = Bandwidth.classic(authRequestsPerMinute, Refill.intervally(authRequestsPerMinute, Duration.ofMinutes(1)));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Add rate limit information to response headers
     */
    private void addRateLimitHeaders(HttpServletResponse response, Bucket bucket) {
        response.setHeader("X-Rate-Limit-Remaining", String.valueOf(bucket.getAvailableTokens()));
        response.setHeader("X-Rate-Limit-Reset", String.valueOf(System.currentTimeMillis() / 1000 + 60));
    }

    /**
     * Send 429 Too Many Requests response
     */
    private void sendRateLimitExceededResponse(HttpServletResponse response) throws IOException {
        response.setStatus(429); // 429 Too Many Requests
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String jsonResponse = """
            {
                "success": false,
                "errorCode": "RATE_LIMIT_EXCEEDED",
                "message": "Too many requests. Please try again later.",
                "timestamp": "%s",
                "retryAfter": 60
            }
            """.formatted(java.time.Instant.now().toString());
        
        response.getWriter().write(jsonResponse);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        
        // Skip rate limiting for health checks and documentation
        return path.contains("/actuator/health") ||
               path.contains("/swagger-ui") ||
               path.contains("/api-docs") ||
               path.contains("/webjars");
    }
}