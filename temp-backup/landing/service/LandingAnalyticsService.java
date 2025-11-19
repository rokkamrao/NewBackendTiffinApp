package com.tiffin.landing.service;

import com.tiffin.landing.dto.LandingAnalyticsDTOs.*;
import com.tiffin.landing.model.LandingAnalytics;
import com.tiffin.landing.repository.LandingAnalyticsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class LandingAnalyticsService {
    
    private static final Logger logger = LoggerFactory.getLogger(LandingAnalyticsService.class);
    
    @Autowired
    private LandingAnalyticsRepository analyticsRepository;
    
    public AnalyticsEventResponse trackEvent(AnalyticsEventRequest request, HttpServletRequest httpRequest) {
        try {
            LandingAnalytics analytics = new LandingAnalytics();
            
            // Map request data
            analytics.setEventType(request.getEventType());
            analytics.setEventCategory(request.getEventCategory());
            analytics.setPageSection(request.getPageSection());
            analytics.setElementId(request.getElementId());
            analytics.setElementText(request.getElementText());
            analytics.setSessionId(request.getSessionId());
            analytics.setDeviceType(request.getDeviceType());
            analytics.setBrowserName(request.getBrowserName());
            analytics.setBrowserVersion(request.getBrowserVersion());
            analytics.setOperatingSystem(request.getOperatingSystem());
            analytics.setScreenResolution(request.getScreenResolution());
            analytics.setViewportSize(request.getViewportSize());
            analytics.setTimeOnPageSeconds(request.getTimeOnPageSeconds());
            analytics.setScrollPercentage(request.getScrollPercentage());
            analytics.setClickX(request.getClickX());
            analytics.setClickY(request.getClickY());
            analytics.setFormData(request.getFormData());
            analytics.setAdditionalData(request.getAdditionalData());
            analytics.setUserId(request.getUserId());
            
            // Extract data from HTTP request
            analytics.setUserAgent(httpRequest.getHeader("User-Agent"));
            analytics.setIpAddress(getClientIpAddress(httpRequest));
            analytics.setReferrer(httpRequest.getHeader("Referer"));
            
            // Extract UTM parameters from request
            analytics.setUtmSource(httpRequest.getParameter("utm_source"));
            analytics.setUtmMedium(httpRequest.getParameter("utm_medium"));
            analytics.setUtmCampaign(httpRequest.getParameter("utm_campaign"));
            analytics.setUtmTerm(httpRequest.getParameter("utm_term"));
            analytics.setUtmContent(httpRequest.getParameter("utm_content"));
            
            // Save analytics event
            LandingAnalytics saved = analyticsRepository.save(analytics);
            logger.info("Analytics event tracked: {} - {}", saved.getEventType(), saved.getId());
            
            return convertToResponse(saved);
        } catch (Exception e) {
            logger.error("Error tracking analytics event: ", e);
            throw new RuntimeException("Failed to track analytics event");
        }
    }
    
    public AnalyticsSummary getAnalyticsSummary() {
        try {
            AnalyticsSummary summary = new AnalyticsSummary();
            
            // Basic metrics
            summary.setTotalPageViews(analyticsRepository.countByEventType("PAGE_VIEW"));
            summary.setUniqueVisitors(analyticsRepository.countUniqueVisitors());
            summary.setTotalClicks(analyticsRepository.countByEventType("BUTTON_CLICK"));
            summary.setNewsletterSignups(analyticsRepository.countByEventType("NEWSLETTER_SIGNUP"));
            summary.setFormSubmissions(analyticsRepository.countByEventType("FORM_SUBMIT"));
            
            // Average metrics
            Double avgTime = analyticsRepository.getAverageTimeOnPage();
            summary.setAverageTimeOnPage(avgTime != null ? avgTime : 0.0);
            
            Double avgScroll = analyticsRepository.getAverageScrollPercentage();
            summary.setAverageScrollPercentage(avgScroll != null ? avgScroll : 0.0);
            
            // Top values
            summary.setTopReferrer(getTopValue(analyticsRepository.getTopReferrers()));
            summary.setTopDevice(getTopValue(analyticsRepository.getTopDevices()));
            summary.setTopBrowser(getTopValue(analyticsRepository.getTopBrowsers()));
            summary.setTopCountry(getTopValue(analyticsRepository.getTopCountries()));
            
            return summary;
        } catch (Exception e) {
            logger.error("Error generating analytics summary: ", e);
            throw new RuntimeException("Failed to generate analytics summary");
        }
    }
    
    public AnalyticsSummary getAnalyticsSummaryByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        try {
            AnalyticsSummary summary = new AnalyticsSummary();
            
            summary.setTotalPageViews(analyticsRepository.countByEventTypeAndDateRange("PAGE_VIEW", startDate, endDate));
            summary.setTotalClicks(analyticsRepository.countByEventTypeAndDateRange("BUTTON_CLICK", startDate, endDate));
            summary.setNewsletterSignups(analyticsRepository.countByEventTypeAndDateRange("NEWSLETTER_SIGNUP", startDate, endDate));
            summary.setFormSubmissions(analyticsRepository.countByEventTypeAndDateRange("FORM_SUBMIT", startDate, endDate));
            
            // Note: For date range queries, we'd need more complex queries to get accurate averages and tops
            // This is a simplified version
            Double avgTime = analyticsRepository.getAverageTimeOnPage();
            summary.setAverageTimeOnPage(avgTime != null ? avgTime : 0.0);
            
            Double avgScroll = analyticsRepository.getAverageScrollPercentage();
            summary.setAverageScrollPercentage(avgScroll != null ? avgScroll : 0.0);
            
            return summary;
        } catch (Exception e) {
            logger.error("Error generating analytics summary for date range: ", e);
            throw new RuntimeException("Failed to generate analytics summary for date range");
        }
    }
    
    public List<LocationAnalytics> getLocationAnalytics() {
        try {
            List<Object[]> results = analyticsRepository.getLocationAnalytics();
            return results.stream()
                    .map(this::convertToLocationAnalytics)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error getting location analytics: ", e);
            throw new RuntimeException("Failed to get location analytics");
        }
    }
    
    public List<PageSectionAnalytics> getPageSectionAnalytics() {
        try {
            List<Object[]> sectionResults = analyticsRepository.getPageSectionAnalytics();
            List<Object[]> clickResults = analyticsRepository.getClickAnalytics();
            
            return sectionResults.stream()
                    .map(result -> {
                        PageSectionAnalytics analytics = new PageSectionAnalytics();
                        analytics.setPageSection((String) result[0]);
                        analytics.setViewCount(((Number) result[1]).longValue());
                        
                        // Find corresponding click data
                        long clickCount = clickResults.stream()
                                .filter(click -> click[0].equals(result[0]))
                                .mapToLong(click -> ((Number) click[1]).longValue())
                                .findFirst()
                                .orElse(0L);
                        
                        analytics.setClickCount(clickCount);
                        
                        // Calculate engagement rate (clicks / views)
                        if (analytics.getViewCount() > 0) {
                            analytics.setEngagementRate((double) clickCount / analytics.getViewCount() * 100);
                        }
                        
                        return analytics;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error getting page section analytics: ", e);
            throw new RuntimeException("Failed to get page section analytics");
        }
    }
    
    public List<AnalyticsEventResponse> getRecentEvents(int limit) {
        try {
            List<LandingAnalytics> events = analyticsRepository.findRecentEvents();
            return events.stream()
                    .limit(limit)
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error getting recent events: ", e);
            throw new RuntimeException("Failed to get recent events");
        }
    }
    
    public List<AnalyticsEventResponse> getEventsBySession(String sessionId) {
        try {
            List<LandingAnalytics> events = analyticsRepository.findBySessionIdOrderByCreatedAtAsc(sessionId);
            return events.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error getting events by session: ", e);
            throw new RuntimeException("Failed to get events by session");
        }
    }
    
    public List<AnalyticsEventResponse> getEventsByPageSection(String pageSection) {
        try {
            List<LandingAnalytics> events = analyticsRepository.findByPageSectionOrderByCreatedAtDesc(pageSection);
            return events.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error getting events by page section: ", e);
            throw new RuntimeException("Failed to get events by page section");
        }
    }
    
    // Helper methods
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
    
    private String getTopValue(List<Object[]> results) {
        if (results != null && !results.isEmpty()) {
            Object[] first = results.get(0);
            return first[0] != null ? first[0].toString() : "Unknown";
        }
        return "Unknown";
    }
    
    private LocationAnalytics convertToLocationAnalytics(Object[] result) {
        LocationAnalytics analytics = new LocationAnalytics();
        analytics.setCountry((String) result[0]);
        analytics.setCity((String) result[1]);
        analytics.setVisitorCount(((Number) result[2]).longValue());
        
        if (result[3] != null) {
            analytics.setAverageTimeOnPage(((Number) result[3]).doubleValue());
        }
        
        return analytics;
    }
    
    private AnalyticsEventResponse convertToResponse(LandingAnalytics analytics) {
        AnalyticsEventResponse response = new AnalyticsEventResponse();
        response.setId(analytics.getId());
        response.setEventType(analytics.getEventType());
        response.setEventCategory(analytics.getEventCategory());
        response.setPageSection(analytics.getPageSection());
        response.setElementId(analytics.getElementId());
        response.setElementText(analytics.getElementText());
        response.setSessionId(analytics.getSessionId());
        response.setDeviceType(analytics.getDeviceType());
        response.setBrowserName(analytics.getBrowserName());
        response.setCountry(analytics.getCountry());
        response.setCity(analytics.getCity());
        response.setCreatedAt(analytics.getCreatedAt());
        return response;
    }
}