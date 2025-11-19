package com.tiffin.newsletter.controller;

import com.tiffin.newsletter.dto.NewsletterSubscriptionRequest;
import com.tiffin.newsletter.dto.NewsletterSubscriptionResponse;
import com.tiffin.newsletter.service.NewsletterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/newsletter")
@CrossOrigin(origins = "*")
public class NewsletterController {
    
    @Autowired
    private NewsletterService newsletterService;
    
    @PostMapping("/subscribe")
    public ResponseEntity<NewsletterSubscriptionResponse> subscribe(@Valid @RequestBody NewsletterSubscriptionRequest request, HttpServletRequest httpRequest) {
        String ipAddress = getClientIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");
        NewsletterSubscriptionResponse response = newsletterService.subscribe(request, ipAddress, userAgent);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/unsubscribe") 
    public ResponseEntity<Map<String, String>> unsubscribe(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        boolean success = newsletterService.unsubscribe(email);
        
        Map<String, String> response = Map.of(
            "message", success ? "Successfully unsubscribed" : "Email not found",
            "status", success ? "success" : "error"
        );
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getNewsletterStats() {
        long activeSubscribers = newsletterService.getActiveSubscriptionCount();
        long recentSubscribers = newsletterService.getRecentSubscriptionCount(30);
        
        Map<String, Object> stats = Map.of(
            "activeSubscribers", activeSubscribers,
            "recentSubscribers", recentSubscribers,
            "status", "success"
        );
        
        return ResponseEntity.ok(stats);
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");
        if (xForwardedForHeader == null) {
            return request.getRemoteAddr();
        } else {
            // X-Forwarded-For can contain multiple IP addresses
            return xForwardedForHeader.split(",")[0];
        }
    }
}