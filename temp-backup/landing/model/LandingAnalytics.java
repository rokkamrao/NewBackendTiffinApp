package com.tiffin.landing.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "landing_analytics")
public class LandingAnalytics {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "session_id")
    private String sessionId;
    
    @NotBlank(message = "Event type is required")
    @Column(name = "event_type", nullable = false)
    private String eventType; // PAGE_VIEW, BUTTON_CLICK, FORM_SUBMIT, NEWSLETTER_SIGNUP, etc.
    
    @Column(name = "event_category")
    private String eventCategory; // NAVIGATION, ENGAGEMENT, CONVERSION, etc.
    
    @Column(name = "page_section")
    private String pageSection; // HERO, MENU, TESTIMONIALS, SIGNUP, etc.
    
    @Column(name = "element_id")
    private String elementId; // Specific element identifier
    
    @Column(name = "element_text")
    private String elementText; // Button text, link text, etc.
    
    @Column(name = "user_agent", columnDefinition = "text")
    private String userAgent;
    
    @Column(name = "ip_address")
    private String ipAddress;
    
    @Column(name = "referrer")
    private String referrer;
    
    @Column(name = "utm_source")
    private String utmSource;
    
    @Column(name = "utm_medium")
    private String utmMedium;
    
    @Column(name = "utm_campaign")
    private String utmCampaign;
    
    @Column(name = "utm_term")
    private String utmTerm;
    
    @Column(name = "utm_content")
    private String utmContent;
    
    @Column(name = "device_type")
    private String deviceType; // DESKTOP, MOBILE, TABLET
    
    @Column(name = "browser_name")
    private String browserName;
    
    @Column(name = "browser_version")
    private String browserVersion;
    
    @Column(name = "operating_system")
    private String operatingSystem;
    
    @Column(name = "screen_resolution")
    private String screenResolution;
    
    @Column(name = "viewport_size")
    private String viewportSize;
    
    @Column(name = "country")
    private String country;
    
    @Column(name = "city")
    private String city;
    
    @Column(name = "region")
    private String region;
    
    @Column(name = "latitude")
    private Double latitude;
    
    @Column(name = "longitude")
    private Double longitude;
    
    @Column(name = "time_on_page_seconds")
    private Integer timeOnPageSeconds;
    
    @Column(name = "scroll_percentage")
    private Integer scrollPercentage;
    
    @Column(name = "click_x")
    private Integer clickX;
    
    @Column(name = "click_y")
    private Integer clickY;
    
    @Column(name = "form_data", columnDefinition = "text")
    private String formData; // JSON string of form data
    
    @Column(name = "additional_data", columnDefinition = "text")
    private String additionalData; // JSON string for any additional data
    
    @Column(name = "user_id")
    private Long userId; // If user is logged in
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public LandingAnalytics() {}
    
    public LandingAnalytics(String eventType, String sessionId) {
        this.eventType = eventType;
        this.sessionId = sessionId;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    
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
    
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    
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
    
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    
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
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}