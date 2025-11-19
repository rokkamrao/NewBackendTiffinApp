package com.tiffin.membership.repository;

import com.tiffin.membership.model.UserMembership;
import com.tiffin.membership.model.MembershipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserMembershipRepository extends JpaRepository<UserMembership, Long> {
    
    /**
     * Find current active membership for user
     */
    Optional<UserMembership> findByUserIdAndStatus(@NonNull Long userId, @NonNull MembershipStatus status);
    
    /**
     * Find current active membership for user
     */
    @Query("SELECT um FROM UserMembership um WHERE um.userId = :userId AND um.status = 'ACTIVE' AND um.endDate > CURRENT_TIMESTAMP")
    Optional<UserMembership> findActiveByUserId(@Param("userId") @NonNull Long userId);
    
    /**
     * Find all memberships for user (including expired)
     */
    List<UserMembership> findByUserIdOrderByCreatedAtDesc(@NonNull Long userId);
    
    /**
     * Find memberships expiring soon
     */
    @Query("SELECT um FROM UserMembership um WHERE um.status = 'ACTIVE' AND um.endDate BETWEEN CURRENT_TIMESTAMP AND :expiryDate AND um.autoRenewal = false")
    List<UserMembership> findExpiringSoon(@Param("expiryDate") @NonNull LocalDateTime expiryDate);
    
    /**
     * Find memberships for auto-renewal
     */
    @Query("SELECT um FROM UserMembership um WHERE um.status = 'ACTIVE' AND um.nextBillingDate <= CURRENT_TIMESTAMP AND um.autoRenewal = true")
    List<UserMembership> findForAutoRenewal();
    
    /**
     * Find expired memberships
     */
    @Query("SELECT um FROM UserMembership um WHERE um.status = 'ACTIVE' AND um.endDate < CURRENT_TIMESTAMP")
    List<UserMembership> findExpiredMemberships();
    
    /**
     * Count active memberships by plan
     */
    @Query("SELECT um.plan.id, COUNT(um.id) FROM UserMembership um WHERE um.status = 'ACTIVE' GROUP BY um.plan.id")
    List<Object[]> countActiveMembershipsByPlan();
    
    /**
     * Get membership statistics for date range
     */
    @Query("SELECT DATE(um.createdAt) as date, COUNT(um.id) as count, SUM(um.paidAmount) as revenue " +
           "FROM UserMembership um WHERE um.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY DATE(um.createdAt) ORDER BY date")
    List<Object[]> getMembershipStatsForDateRange(@Param("startDate") @NonNull LocalDateTime startDate, 
                                                  @Param("endDate") @NonNull LocalDateTime endDate);
    
    /**
     * Find users with premium memberships
     */
    @Query("SELECT DISTINCT um.userId FROM UserMembership um JOIN um.plan p WHERE um.status = 'ACTIVE' AND p.tier != 'FREE'")
    List<Long> findPremiumUserIds();
    
    /**
     * Get user's total spent on memberships
     */
    @Query("SELECT COALESCE(SUM(um.paidAmount), 0) FROM UserMembership um WHERE um.userId = :userId")
    @Nullable java.math.BigDecimal getTotalSpentByUser(@Param("userId") @NonNull Long userId);
    
    /**
     * Find membership by transaction ID
     */
    Optional<UserMembership> findByPaymentTransactionId(@NonNull String transactionId);
    
    /**
     * Check if user has ever had a membership
     */
    boolean existsByUserId(@NonNull Long userId);
    
    /**
     * Get most popular plans
     */
    @Query("SELECT um.plan, COUNT(um.id) as subscriptionCount FROM UserMembership um " +
           "WHERE um.createdAt >= :startDate GROUP BY um.plan ORDER BY subscriptionCount DESC")
    List<Object[]> getMostPopularPlans(@Param("startDate") @NonNull LocalDateTime startDate);
}