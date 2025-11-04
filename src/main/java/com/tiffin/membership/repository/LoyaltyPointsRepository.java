package com.tiffin.membership.repository;

import com.tiffin.membership.model.LoyaltyPoints;
import com.tiffin.membership.model.LoyaltyTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoyaltyPointsRepository extends JpaRepository<LoyaltyPoints, Long> {
    
    /**
     * Find loyalty points by user ID
     */
    Optional<LoyaltyPoints> findByUserId(@NonNull Long userId);
    
    /**
     * Find users by loyalty tier
     */
    List<LoyaltyPoints> findByTierOrderByTotalPointsDesc(@NonNull LoyaltyTier tier);
    
    /**
     * Find top users by points
     */
    List<LoyaltyPoints> findTop10ByOrderByTotalPointsDesc();
    
    /**
     * Get loyalty leaderboard
     */
    @Query("SELECT lp FROM LoyaltyPoints lp ORDER BY lp.totalPoints DESC, lp.lifetimeEarned DESC")
    List<LoyaltyPoints> findLeaderboard();
    
    /**
     * Count users by tier
     */
    @Query("SELECT lp.tier, COUNT(lp.id) FROM LoyaltyPoints lp GROUP BY lp.tier")
    List<Object[]> countUsersByTier();
    
    /**
     * Get total points in system
     */
    @Query("SELECT SUM(lp.totalPoints) FROM LoyaltyPoints lp")
    Long getTotalPointsInSystem();
    
    /**
     * Find users with points expiring soon
     */
    @Query("SELECT lp FROM LoyaltyPoints lp WHERE lp.nextTierDate IS NOT NULL AND lp.nextTierDate <= CURRENT_TIMESTAMP")
    List<LoyaltyPoints> findUsersEligibleForTierUpgrade();
    
    /**
     * Get average points by tier
     */
    @Query("SELECT lp.tier, AVG(lp.totalPoints) FROM LoyaltyPoints lp GROUP BY lp.tier")
    List<Object[]> getAveragePointsByTier();
}