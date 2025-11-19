package com.tiffin.landing.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

public class LandingAnalyticsDTOs {
    
    // Request DTO for tracking analytics events
    public static class AnalyticsEventRequest {
        
        @NotBlank(message = "Event type is required")
        private String eventType;
        
        private String eventCategory;
        private String pageSection;
        private String elementId;
        private String elementText;
        private String sessionId;
        private String userAgent;
        private String referrer;
        private String utmSource;
        private String utmMedium;
        private String utmCampaign;
        private String utmTerm;
        private String utmContent;
        private String deviceType;
        private String browserName;
        private String browserVersion;
        private String operatingSystem;
        private String screenResolution;
        private String viewportSize;
        private Integer timeOnPageSeconds;
        private Integer scrollPercentage;
        private Integer clickX;
        private Integer clickY;
        private String formData;
        private String additionalData;
        private Long userId;
        
        // Getters and Setters
        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }
        
        public String getEventCategory() { return eventCategory; }
        public void setEventCategory(String eventCategory) { this.eventCategory = eventCategory; }
        
        public String getPageSection() { return pageSection; }
        public void setPageSection(String pageSection) { this.pageSection = pageSection; }
        
        public String getElementId() { return elementId; }
        public void setElementId(String elementId) { this.elementId = elementId; }
        
        public String getElementText() { return elementText; }
        public void setElementText(String elementText) { this.elementText = elementText; }
        
        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }
        
        public String getUserAgent() { return userAgent; }
        public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
        
        public String getReferrer() { return referrer; }
        public void setReferrer(String referrer) { this.referrer = referrer; }
        
        public String getUtmSource() { return utmSource; }
        public void setUtmSource(String utmSource) { this.utmSource = utmSource; }
        
        public String getUtmMedium() { return utmMedium; }
        public void setUtmMedium(String utmMedium) { this.utmMedium = utmMedium; }
        
        public String getUtmCampaign() { return utmCampaign; }
        public void setUtmCampaign(String utmCampaign) { this.utmCampaign = utmCampaign; }
        
        public String getUtmTerm() { return utmTerm; }
        public void setUtmTerm(String utmTerm) { this.utmTerm = utmTerm; }
        
        public String getUtmContent() { return utmContent; }
        public void setUtmContent(String utmContent) { this.utmContent = utmContent; }
        
        public String getDeviceType() { return deviceType; }
        public void setDeviceType(String deviceType) { this.deviceType = deviceType; }
        
        public String getBrowserName() { return browserName; }
        public void setBrowserName(String browserName) { this.browserName = browserName; }
        
        public String getBrowserVersion() { return browserVersion; }
        public void setBrowserVersion(String browserVersion) { this.browserVersion = browserVersion; }
        
        public String getOperatingSystem() { return operatingSystem; }
        public void setOperatingSystem(String operatingSystem) { this.operatingSystem = operatingSystem; }
        
        public String getScreenResolution() { return screenResolution; }
        public void setScreenResolution(String screenResolution) { this.screenResolution = screenResolution; }
        
        public String getViewportSize() { return viewportSize; }
        public void setViewportSize(String viewportSize) { this.viewportSize = viewportSize; }
        
        public Integer getTimeOnPageSeconds() { return timeOnPageSeconds; }
        public void setTimeOnPageSeconds(Integer timeOnPageSeconds) { this.timeOnPageSeconds = timeOnPageSeconds; }
        
        public Integer getScrollPercentage() { return scrollPercentage; }
        public void setScrollPercentage(Integer scrollPercentage) { this.scrollPercentage = scrollPercentage; }
        
        public Integer getClickX() { return clickX; }
        public void setClickX(Integer clickX) { this.clickX = clickX; }
        
        public Integer getClickY() { return clickY; }
        public void setClickY(Integer clickY) { this.clickY = clickY; }
        
        public String getFormData() { return formData; }
        public void setFormData(String formData) { this.formData = formData; }
        
        public String getAdditionalData() { return additionalData; }
        public void setAdditionalData(String additionalData) { this.additionalData = additionalData; }
        
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
    }
    
    // Response DTO for analytics events
    public static class AnalyticsEventResponse {
        
        private Long id;
        private String eventType;
        private String eventCategory;
        private String pageSection;
        private String elementId;
        private String elementText;
        private String sessionId;
        private String deviceType;
        private String browserName;
        private String country;
        private String city;
        private LocalDateTime createdAt;
        
        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }
        
        public String getEventCategory() { return eventCategory; }
        public void setEventCategory(String eventCategory) { this.eventCategory = eventCategory; }
        
        public String getPageSection() { return pageSection; }
        public void setPageSection(String pageSection) { this.pageSection = pageSection; }
        
        public String getElementId() { return elementId; }
        public void setElementId(String elementId) { this.elementId = elementId; }
        
        public String getElementText() { return elementText; }
        public void setElementText(String elementText) { this.elementText = elementText; }
        
        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }
        
        public String getDeviceType() { return deviceType; }
        public void setDeviceType(String deviceType) { this.deviceType = deviceType; }
        
        public String getBrowserName() { return browserName; }
        public void setBrowserName(String browserName) { this.browserName = browserName; }
        
        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }
        
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    }
    
    // Analytics summary for reporting
    public static class AnalyticsSummary {
        
        private long totalPageViews;
        private long uniqueVisitors;
        private long totalClicks;
        private long newsletterSignups;
        private long formSubmissions;
        private double averageTimeOnPage;
        private double averageScrollPercentage;
        private String topReferrer;
        private String topDevice;
        private String topBrowser;
        private String topCountry;
        
        // Getters and Setters
        public long getTotalPageViews() { return totalPageViews; }
        public void setTotalPageViews(long totalPageViews) { this.totalPageViews = totalPageViews; }
        
        public long getUniqueVisitors() { return uniqueVisitors; }
        public void setUniqueVisitors(long uniqueVisitors) { this.uniqueVisitors = uniqueVisitors; }
        
        public long getTotalClicks() { return totalClicks; }
        public void setTotalClicks(long totalClicks) { this.totalClicks = totalClicks; }
        
        public long getNewsletterSignups() { return newsletterSignups; }
        public void setNewsletterSignups(long newsletterSignups) { this.newsletterSignups = newsletterSignups; }
        
        public long getFormSubmissions() { return formSubmissions; }
        public void setFormSubmissions(long formSubmissions) { this.formSubmissions = formSubmissions; }
        
        public double getAverageTimeOnPage() { return averageTimeOnPage; }
        public void setAverageTimeOnPage(double averageTimeOnPage) { this.averageTimeOnPage = averageTimeOnPage; }
        
        public double getAverageScrollPercentage() { return averageScrollPercentage; }
        public void setAverageScrollPercentage(double averageScrollPercentage) { this.averageScrollPercentage = averageScrollPercentage; }
        
        public String getTopReferrer() { return topReferrer; }
        public void setTopReferrer(String topReferrer) { this.topReferrer = topReferrer; }
        
        public String getTopDevice() { return topDevice; }
        public void setTopDevice(String topDevice) { this.topDevice = topDevice; }
        
        public String getTopBrowser() { return topBrowser; }
        public void setTopBrowser(String topBrowser) { this.topBrowser = topBrowser; }
        
        public String getTopCountry() { return topCountry; }
        public void setTopCountry(String topCountry) { this.topCountry = topCountry; }
    }
    
    // Location-based analytics
    public static class LocationAnalytics {
        
        private String country;
        private String city;
        private String region;
        private long visitorCount;
        private double averageTimeOnPage;
        private long conversions;
        
        // Getters and Setters
        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }
        
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        
        public String getRegion() { return region; }
        public void setRegion(String region) { this.region = region; }
        
        public long getVisitorCount() { return visitorCount; }
        public void setVisitorCount(long visitorCount) { this.visitorCount = visitorCount; }
        
        public double getAverageTimeOnPage() { return averageTimeOnPage; }
        public void setAverageTimeOnPage(double averageTimeOnPage) { this.averageTimeOnPage = averageTimeOnPage; }
        
        public long getConversions() { return conversions; }
        public void setConversions(long conversions) { this.conversions = conversions; }
    }
    
    // Page section analytics
    public static class PageSectionAnalytics {
        
        private String pageSection;
        private long viewCount;
        private long clickCount;
        private double engagementRate;
        private double conversionRate;
        
        // Getters and Setters
        public String getPageSection() { return pageSection; }
        public void setPageSection(String pageSection) { this.pageSection = pageSection; }
        
        public long getViewCount() { return viewCount; }
        public void setViewCount(long viewCount) { this.viewCount = viewCount; }
        
        public long getClickCount() { return clickCount; }
        public void setClickCount(long clickCount) { this.clickCount = clickCount; }
        
        public double getEngagementRate() { return engagementRate; }
        public void setEngagementRate(double engagementRate) { this.engagementRate = engagementRate; }
        
        public double getConversionRate() { return conversionRate; }
        public void setConversionRate(double conversionRate) { this.conversionRate = conversionRate; }
    }
}