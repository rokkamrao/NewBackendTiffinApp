package com.tiffin.newsletter.service;

import com.tiffin.newsletter.dto.NewsletterSubscriptionRequest;
import com.tiffin.newsletter.dto.NewsletterSubscriptionResponse;
import com.tiffin.newsletter.model.NewsletterSubscription;
import com.tiffin.newsletter.repository.NewsletterSubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class NewsletterService {
    
    private static final Logger logger = LoggerFactory.getLogger(NewsletterService.class);
    
    @Autowired
    private NewsletterSubscriptionRepository newsletterRepository;
    
    public NewsletterSubscriptionResponse subscribe(NewsletterSubscriptionRequest request, String ipAddress, String userAgent) {
        try {
            String email = request.getEmail().toLowerCase().trim();
            
            // Check if already subscribed
            Optional<NewsletterSubscription> existing = newsletterRepository.findByEmail(email);
            if (existing.isPresent()) {
                if (existing.get().getIsActive()) {
                    logger.info("Newsletter subscription attempt for already subscribed email: {}", email);
                    return NewsletterSubscriptionResponse.alreadySubscribed(email);
                } else {
                    // Reactivate subscription
                    NewsletterSubscription subscription = existing.get();
                    subscription.setIsActive(true);
                    subscription.setSubscribedAt(LocalDateTime.now());
                    subscription.setIpAddress(ipAddress);
                    subscription.setUserAgent(userAgent);
                    newsletterRepository.save(subscription);
                    
                    logger.info("Newsletter subscription reactivated for email: {}", email);
                    return NewsletterSubscriptionResponse.success(email);
                }
            }
            
            // Create new subscription
            NewsletterSubscription subscription = new NewsletterSubscription(email);
            subscription.setSource(request.getSource());
            subscription.setIpAddress(ipAddress);
            subscription.setUserAgent(userAgent);
            
            newsletterRepository.save(subscription);
            
            logger.info("New newsletter subscription created for email: {}", email);
            return NewsletterSubscriptionResponse.success(email);
            
        } catch (Exception e) {
            logger.error("Error subscribing to newsletter: ", e);
            return NewsletterSubscriptionResponse.error("An error occurred while subscribing. Please try again.");
        }
    }
    
    public boolean unsubscribe(String email) {
        try {
            Optional<NewsletterSubscription> subscription = newsletterRepository.findByEmail(email.toLowerCase().trim());
            if (subscription.isPresent()) {
                subscription.get().setIsActive(false);
                newsletterRepository.save(subscription.get());
                logger.info("Newsletter subscription deactivated for email: {}", email);
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("Error unsubscribing from newsletter: ", e);
            return false;
        }
    }
    
    public List<NewsletterSubscription> getActiveSubscriptions() {
        return newsletterRepository.findByIsActiveTrue();
    }
    
    public long getActiveSubscriptionCount() {
        return newsletterRepository.countActiveSubscriptions();
    }
    
    public long getRecentSubscriptionCount(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return newsletterRepository.countSubscriptionsSince(since);
    }
    
    public List<NewsletterSubscription> getRecentSubscriptions(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return newsletterRepository.findRecentSubscriptions(since);
    }
}