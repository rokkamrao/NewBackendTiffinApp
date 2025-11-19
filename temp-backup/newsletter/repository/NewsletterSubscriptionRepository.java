package com.tiffin.newsletter.repository;

import com.tiffin.newsletter.model.NewsletterSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NewsletterSubscriptionRepository extends JpaRepository<NewsletterSubscription, Long> {
    
    Optional<NewsletterSubscription> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    List<NewsletterSubscription> findByIsActiveTrue();
    
    @Query("SELECT COUNT(n) FROM NewsletterSubscription n WHERE n.isActive = true")
    long countActiveSubscriptions();
    
    @Query("SELECT COUNT(n) FROM NewsletterSubscription n WHERE n.subscribedAt >= :since")
    long countSubscriptionsSince(LocalDateTime since);
    
    @Query("SELECT n FROM NewsletterSubscription n WHERE n.subscribedAt >= :since ORDER BY n.subscribedAt DESC")
    List<NewsletterSubscription> findRecentSubscriptions(LocalDateTime since);
}