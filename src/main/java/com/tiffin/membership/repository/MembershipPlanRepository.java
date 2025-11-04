package com.tiffin.membership.repository;

import com.tiffin.membership.model.MembershipPlan;
import com.tiffin.membership.model.MembershipTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

@Repository
public interface MembershipPlanRepository extends JpaRepository<MembershipPlan, Long> {
    
    /**
     * Find all active membership plans
     */
    List<MembershipPlan> findByIsActiveTrueOrderByTier();
    
    /**
     * Find featured membership plans
     */
    List<MembershipPlan> findByIsFeaturedTrueAndIsActiveTrueOrderByTier();
    
    /**
     * Find plan by name
     */
    Optional<MembershipPlan> findByNameAndIsActiveTrue(@NonNull String name);
    
    /**
     * Find plans by tier
     */
    List<MembershipPlan> findByTierAndIsActiveTrueOrderByMonthlyPrice(@NonNull MembershipTier tier);
    
    /**
     * Get plans ordered by price
     */
    @Query("SELECT p FROM MembershipPlan p WHERE p.isActive = true ORDER BY p.monthlyPrice ASC")
    List<MembershipPlan> findAllActiveOrderedByPrice();
    
    /**
     * Find plans within price range
     */
    @Query("SELECT p FROM MembershipPlan p WHERE p.isActive = true AND p.monthlyPrice BETWEEN :minPrice AND :maxPrice ORDER BY p.monthlyPrice")
    List<MembershipPlan> findByPriceRange(@Param("minPrice") java.math.BigDecimal minPrice, 
                                         @Param("maxPrice") java.math.BigDecimal maxPrice);
    
    /**
     * Get plan usage statistics
     */
    @Query("SELECT p.id, p.name, COUNT(um.id) as subscriptionCount FROM MembershipPlan p " +
           "LEFT JOIN UserMembership um ON p.id = um.plan.id AND um.status = 'ACTIVE' " +
           "WHERE p.isActive = true GROUP BY p.id, p.name ORDER BY subscriptionCount DESC")
    List<Object[]> getPlanPopularityStats();
}