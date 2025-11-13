package com.tiffin.landing.controller;

import com.tiffin.landing.dto.LandingAnalyticsDTOs.*;
import com.tiffin.landing.service.LandingAnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/analytics")
@CrossOrigin(origins = "*")
public class LandingAnalyticsController {
    
    @Autowired
    private LandingAnalyticsService analyticsService;
    
    @PostMapping("/track")
    public ResponseEntity<AnalyticsEventResponse> trackEvent(
            @Valid @RequestBody AnalyticsEventRequest request,
            HttpServletRequest httpRequest) {
        AnalyticsEventResponse response = analyticsService.trackEvent(request, httpRequest);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/summary")
    public ResponseEntity<AnalyticsSummary> getAnalyticsSummary() {
        AnalyticsSummary summary = analyticsService.getAnalyticsSummary();
        return ResponseEntity.ok(summary);
    }
    
    @GetMapping("/summary/date-range")
    public ResponseEntity<AnalyticsSummary> getAnalyticsSummaryByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        AnalyticsSummary summary = analyticsService.getAnalyticsSummaryByDateRange(startDate, endDate);
        return ResponseEntity.ok(summary);
    }
    
    @GetMapping("/location")
    public ResponseEntity<List<LocationAnalytics>> getLocationAnalytics() {
        List<LocationAnalytics> analytics = analyticsService.getLocationAnalytics();
        return ResponseEntity.ok(analytics);
    }
    
    @GetMapping("/page-sections")
    public ResponseEntity<List<PageSectionAnalytics>> getPageSectionAnalytics() {
        List<PageSectionAnalytics> analytics = analyticsService.getPageSectionAnalytics();
        return ResponseEntity.ok(analytics);
    }
    
    @GetMapping("/recent")
    public ResponseEntity<List<AnalyticsEventResponse>> getRecentEvents(
            @RequestParam(defaultValue = "50") int limit) {
        List<AnalyticsEventResponse> events = analyticsService.getRecentEvents(limit);
        return ResponseEntity.ok(events);
    }
    
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<List<AnalyticsEventResponse>> getEventsBySession(@PathVariable String sessionId) {
        List<AnalyticsEventResponse> events = analyticsService.getEventsBySession(sessionId);
        return ResponseEntity.ok(events);
    }
    
    @GetMapping("/page-section/{pageSection}")
    public ResponseEntity<List<AnalyticsEventResponse>> getEventsByPageSection(@PathVariable String pageSection) {
        List<AnalyticsEventResponse> events = analyticsService.getEventsByPageSection(pageSection);
        return ResponseEntity.ok(events);
    }
    
    // Convenience endpoints for specific tracking
    @PostMapping("/page-view")
    public ResponseEntity<Map<String, String>> trackPageView(
            @RequestParam String sessionId,
            @RequestParam(required = false) String pageSection,
            HttpServletRequest httpRequest) {
        
        AnalyticsEventRequest request = new AnalyticsEventRequest();
        request.setEventType("PAGE_VIEW");
        request.setEventCategory("NAVIGATION");
        request.setSessionId(sessionId);
        request.setPageSection(pageSection);
        
        analyticsService.trackEvent(request, httpRequest);
        
        Map<String, String> response = Map.of(
            "message", "Page view tracked successfully",
            "status", "success"
        );
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/button-click")
    public ResponseEntity<Map<String, String>> trackButtonClick(
            @RequestParam String sessionId,
            @RequestParam String elementId,
            @RequestParam(required = false) String elementText,
            @RequestParam(required = false) String pageSection,
            @RequestParam(required = false) Integer clickX,
            @RequestParam(required = false) Integer clickY,
            HttpServletRequest httpRequest) {
        
        AnalyticsEventRequest request = new AnalyticsEventRequest();
        request.setEventType("BUTTON_CLICK");
        request.setEventCategory("ENGAGEMENT");
        request.setSessionId(sessionId);
        request.setElementId(elementId);
        request.setElementText(elementText);
        request.setPageSection(pageSection);
        request.setClickX(clickX);
        request.setClickY(clickY);
        
        analyticsService.trackEvent(request, httpRequest);
        
        Map<String, String> response = Map.of(
            "message", "Button click tracked successfully",
            "status", "success"
        );
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/newsletter-signup")
    public ResponseEntity<Map<String, String>> trackNewsletterSignup(
            @RequestParam String sessionId,
            @RequestParam String email,
            @RequestParam(required = false) String pageSection,
            HttpServletRequest httpRequest) {
        
        AnalyticsEventRequest request = new AnalyticsEventRequest();
        request.setEventType("NEWSLETTER_SIGNUP");
        request.setEventCategory("CONVERSION");
        request.setSessionId(sessionId);
        request.setPageSection(pageSection);
        request.setFormData("{\"email\":\"" + email + "\"}");
        
        analyticsService.trackEvent(request, httpRequest);
        
        Map<String, String> response = Map.of(
            "message", "Newsletter signup tracked successfully",
            "status", "success"
        );
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/form-submit")
    public ResponseEntity<Map<String, String>> trackFormSubmit(
            @RequestParam String sessionId,
            @RequestParam String formType,
            @RequestParam(required = false) String pageSection,
            @RequestParam(required = false) String formData,
            HttpServletRequest httpRequest) {
        
        AnalyticsEventRequest request = new AnalyticsEventRequest();
        request.setEventType("FORM_SUBMIT");
        request.setEventCategory("CONVERSION");
        request.setSessionId(sessionId);
        request.setPageSection(pageSection);
        request.setElementId(formType);
        request.setFormData(formData);
        
        analyticsService.trackEvent(request, httpRequest);
        
        Map<String, String> response = Map.of(
            "message", "Form submission tracked successfully",
            "status", "success"
        );
        
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/scroll-tracking")
    public ResponseEntity<Map<String, String>> trackScrollPercentage(
            @RequestParam String sessionId,
            @RequestParam Integer scrollPercentage,
            @RequestParam(required = false) Integer timeOnPageSeconds,
            @RequestParam(required = false) String pageSection,
            HttpServletRequest httpRequest) {
        
        AnalyticsEventRequest request = new AnalyticsEventRequest();
        request.setEventType("SCROLL_TRACKING");
        request.setEventCategory("ENGAGEMENT");
        request.setSessionId(sessionId);
        request.setScrollPercentage(scrollPercentage);
        request.setTimeOnPageSeconds(timeOnPageSeconds);
        request.setPageSection(pageSection);
        
        analyticsService.trackEvent(request, httpRequest);
        
        Map<String, String> response = Map.of(
            "message", "Scroll tracking recorded successfully",
            "status", "success"
        );
        
        return ResponseEntity.ok(response);
    }
}