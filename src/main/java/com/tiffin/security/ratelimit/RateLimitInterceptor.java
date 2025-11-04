package com.tiffin.security.ratelimit;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import org.springframework.lang.NonNull;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;

import lombok.extern.slf4j.Slf4j;

/**
 * Rate limiting interceptor for API requests
 */
@Component
@Slf4j
public class RateLimitInterceptor implements HandlerInterceptor {
    
    @Autowired
    private InMemoryRateLimitingService rateLimitService;
    
    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, 
                           @NonNull Object handler) throws Exception {
        
        String ipAddress = getClientIpAddress(request);
        String userId = getUserId(request);
        String requestUri = request.getRequestURI();
        
        // Check if IP is blocked
        if (rateLimitService.isIpBlocked(ipAddress)) {
            log.warn("Request from blocked IP: {} to {}", ipAddress, requestUri);
            setRateLimitResponse(response, "IP temporarily blocked", HttpStatus.TOO_MANY_REQUESTS);
            return false;
        }
        
        // Special handling for login endpoints
        if (isLoginEndpoint(requestUri)) {
            if (!rateLimitService.isLoginAllowed(ipAddress)) {
                log.warn("Login rate limit exceeded for IP: {}", ipAddress);
                
                // Block IP after too many failed login attempts
                rateLimitService.blockIp(ipAddress, Duration.ofMinutes(30));
                
                setRateLimitResponse(response, "Too many login attempts. IP blocked temporarily.", 
                                   HttpStatus.TOO_MANY_REQUESTS);
                return false;
            }
        }
        
        // Check general IP rate limit
        if (!rateLimitService.isIpAllowed(ipAddress)) {
            log.warn("IP rate limit exceeded for: {} accessing {}", ipAddress, requestUri);
            setRateLimitResponse(response, "Rate limit exceeded for IP", HttpStatus.TOO_MANY_REQUESTS);
            return false;
        }
        
        // Check user-specific rate limit (if authenticated)
        if (userId != null && !rateLimitService.isApiCallAllowed(userId)) {
            log.warn("API rate limit exceeded for user: {} accessing {}", userId, requestUri);
            setRateLimitResponse(response, "API rate limit exceeded", HttpStatus.TOO_MANY_REQUESTS);
            return false;
        }
        
        return true;
    }
    
    /**
     * Extract client IP address from request
     */
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
     * Extract user ID from request (from JWT token or session)
     */
    private String getUserId(HttpServletRequest request) {
        try {
            // Try to get user ID from JWT token
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                // In a real implementation, you would decode the JWT token here
                // For now, return null to skip user-specific rate limiting
                return null;
            }
            
            // Try to get user ID from session
            Object userId = request.getSession(false) != null ? 
                          request.getSession(false).getAttribute("userId") : null;
            
            return userId != null ? userId.toString() : null;
        } catch (Exception e) {
            log.debug("Could not extract user ID from request", e);
            return null;
        }
    }
    
    /**
     * Check if the request is to a login endpoint
     */
    private boolean isLoginEndpoint(String requestUri) {
        return requestUri != null && (
            requestUri.contains("/auth/login") ||
            requestUri.contains("/api/auth/login") ||
            requestUri.contains("/login")
        );
    }
    
    /**
     * Set rate limit exceeded response
     */
    private void setRateLimitResponse(HttpServletResponse response, String message, 
                                    HttpStatus status) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String jsonResponse = String.format(
            "{\"error\": \"%s\", \"message\": \"%s\", \"timestamp\": %d}", 
            status.getReasonPhrase(), message, System.currentTimeMillis()
        );
        
        response.getWriter().write(jsonResponse);
        response.getWriter().flush();
    }
}