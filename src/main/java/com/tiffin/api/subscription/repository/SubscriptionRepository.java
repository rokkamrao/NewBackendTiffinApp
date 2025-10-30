package com.tiffin.api.subscription.repository;

import com.tiffin.api.subscription.model.Subscription;
import com.tiffin.api.subscription.model.SubscriptionStatus;
import com.tiffin.api.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findByUserAndStatus(User user, SubscriptionStatus status);
    List<Subscription> findByUserOrderByStartDateDesc(User user);
    long countByStatus(SubscriptionStatus status);
}