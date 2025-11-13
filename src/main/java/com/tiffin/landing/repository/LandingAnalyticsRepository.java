package com.tiffin.landing.repository;

import com.tiffin.landing.model.LandingAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LandingAnalyticsRepository extends JpaRepository<LandingAnalytics, Long> {
    
    // Count total page views
    long countByEventType(String eventType);
    
    // Count unique sessions (visitors)
    @Query("SELECT COUNT(DISTINCT la.sessionId) FROM LandingAnalytics la WHERE la.eventType = 'PAGE_VIEW'")
    long countUniqueVisitors();
    
    // Count events by type and date range
    @Query("SELECT COUNT(la) FROM LandingAnalytics la WHERE la.eventType = :eventType AND la.createdAt BETWEEN :startDate AND :endDate")
    long countByEventTypeAndDateRange(@Param("eventType") String eventType, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    // Get average time on page
    @Query("SELECT AVG(la.timeOnPageSeconds) FROM LandingAnalytics la WHERE la.timeOnPageSeconds IS NOT NULL")
    Double getAverageTimeOnPage();
    
    // Get average scroll percentage
    @Query("SELECT AVG(la.scrollPercentage) FROM LandingAnalytics la WHERE la.scrollPercentage IS NOT NULL")
    Double getAverageScrollPercentage();
    
    // Get top referrers
    @Query("SELECT la.referrer, COUNT(la) as count FROM LandingAnalytics la WHERE la.referrer IS NOT NULL AND la.eventType = 'PAGE_VIEW' GROUP BY la.referrer ORDER BY count DESC")
    List<Object[]> getTopReferrers();
    
    // Get top devices
    @Query("SELECT la.deviceType, COUNT(la) as count FROM LandingAnalytics la WHERE la.deviceType IS NOT NULL GROUP BY la.deviceType ORDER BY count DESC")
    List<Object[]> getTopDevices();
    
    // Get top browsers
    @Query("SELECT la.browserName, COUNT(la) as count FROM LandingAnalytics la WHERE la.browserName IS NOT NULL GROUP BY la.browserName ORDER BY count DESC")
    List<Object[]> getTopBrowsers();
    
    // Get top countries
    @Query("SELECT la.country, COUNT(DISTINCT la.sessionId) as count FROM LandingAnalytics la WHERE la.country IS NOT NULL GROUP BY la.country ORDER BY count DESC")
    List<Object[]> getTopCountries();
    
    // Get analytics by page section
    @Query("SELECT la.pageSection, COUNT(la) as count FROM LandingAnalytics la WHERE la.pageSection IS NOT NULL GROUP BY la.pageSection ORDER BY count DESC")
    List<Object[]> getPageSectionAnalytics();
    
    // Get click events by element
    @Query("SELECT la.elementId, COUNT(la) as count FROM LandingAnalytics la WHERE la.eventType = 'BUTTON_CLICK' AND la.elementId IS NOT NULL GROUP BY la.elementId ORDER BY count DESC")
    List<Object[]> getClickAnalytics();
    
    // Get location-based analytics
    @Query("SELECT la.country, la.city, COUNT(DISTINCT la.sessionId) as visitorCount, AVG(la.timeOnPageSeconds) as avgTime FROM LandingAnalytics la WHERE la.country IS NOT NULL GROUP BY la.country, la.city ORDER BY visitorCount DESC")
    List<Object[]> getLocationAnalytics();
    
    // Get conversion events (newsletter signups, form submissions)
    @Query("SELECT COUNT(la) FROM LandingAnalytics la WHERE la.eventType IN ('NEWSLETTER_SIGNUP', 'FORM_SUBMIT')")
    long countConversions();
    
    // Get events by session
    List<LandingAnalytics> findBySessionIdOrderByCreatedAtAsc(String sessionId);
    
    // Get events by user
    List<LandingAnalytics> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    // Get events by date range
    List<LandingAnalytics> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime startDate, LocalDateTime endDate);
    
    // Get events by page section
    List<LandingAnalytics> findByPageSectionOrderByCreatedAtDesc(String pageSection);
    
    // Get recent events
    @Query("SELECT la FROM LandingAnalytics la ORDER BY la.createdAt DESC")
    List<LandingAnalytics> findRecentEvents();
    
    // Get utm campaign analytics
    @Query("SELECT la.utmCampaign, COUNT(DISTINCT la.sessionId) as visitors, COUNT(la) as events FROM LandingAnalytics la WHERE la.utmCampaign IS NOT NULL GROUP BY la.utmCampaign ORDER BY visitors DESC")
    List<Object[]> getUtmCampaignAnalytics();
    
    // Get utm source analytics
    @Query("SELECT la.utmSource, COUNT(DISTINCT la.sessionId) as visitors, COUNT(la) as events FROM LandingAnalytics la WHERE la.utmSource IS NOT NULL GROUP BY la.utmSource ORDER BY visitors DESC")
    List<Object[]> getUtmSourceAnalytics();
    
    // Get funnel analysis - page views to conversions by session
    @Query("SELECT COUNT(DISTINCT CASE WHEN la.eventType = 'PAGE_VIEW' THEN la.sessionId END) as pageViews, " +
           "COUNT(DISTINCT CASE WHEN la.eventType = 'NEWSLETTER_SIGNUP' THEN la.sessionId END) as signups " +
           "FROM LandingAnalytics la")
    Object[] getFunnelAnalysis();
    
    // Get bounce rate (sessions with only one page view)
    // Fixed: Replaced subquery with alias syntax that was causing compilation errors
    // Original query used invalid alias in subquery - rewrote using proper JPA syntax
    @Query("SELECT COUNT(DISTINCT la.sessionId) FROM LandingAnalytics la WHERE la.eventType = 'PAGE_VIEW' " +
           "AND la.sessionId IN (SELECT la2.sessionId FROM LandingAnalytics la2 WHERE la2.eventType = 'PAGE_VIEW' " +
           "GROUP BY la2.sessionId HAVING COUNT(la2) = 1)")
    long getBounceCount();
    
    // Get operating system analytics
    @Query("SELECT la.operatingSystem, COUNT(DISTINCT la.sessionId) as count FROM LandingAnalytics la WHERE la.operatingSystem IS NOT NULL GROUP BY la.operatingSystem ORDER BY count DESC")
    List<Object[]> getOperatingSystemAnalytics();
    
    // Get screen resolution analytics
    @Query("SELECT la.screenResolution, COUNT(DISTINCT la.sessionId) as count FROM LandingAnalytics la WHERE la.screenResolution IS NOT NULL GROUP BY la.screenResolution ORDER BY count DESC")
    List<Object[]> getScreenResolutionAnalytics();
    
    // Get hourly analytics
    @Query("SELECT HOUR(la.createdAt) as hour, COUNT(la) as count FROM LandingAnalytics la WHERE la.eventType = 'PAGE_VIEW' GROUP BY HOUR(la.createdAt) ORDER BY hour")
    List<Object[]> getHourlyAnalytics();
    
    // Get daily analytics for the last 30 days
    @Query("SELECT DATE(la.createdAt) as date, COUNT(DISTINCT la.sessionId) as visitors, COUNT(la) as pageViews " +
           "FROM LandingAnalytics la WHERE la.createdAt >= :startDate AND la.eventType = 'PAGE_VIEW' " +
           "GROUP BY DATE(la.createdAt) ORDER BY date DESC")
    List<Object[]> getDailyAnalytics(@Param("startDate") LocalDateTime startDate);
}