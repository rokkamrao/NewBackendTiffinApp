package com.tiffin.user.repository;

import com.tiffin.user.model.Role;
import com.tiffin.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Basic queries
    Optional<User> findByEmail(String email);
    
    Optional<User> findByPhoneNumber(String phoneNumber);
    
    boolean existsByEmail(String email);
    
    boolean existsByPhoneNumber(String phoneNumber);
    
    // Validation queries for updates
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.id != :id")
    Optional<User> findByEmailAndIdNot(@Param("email") String email, @Param("id") Long id);
    
    @Query("SELECT u FROM User u WHERE u.phoneNumber = :phoneNumber AND u.id != :id")
    Optional<User> findByPhoneNumberAndIdNot(@Param("phoneNumber") String phoneNumber, @Param("id") Long id);
    
    // Role-based queries
    List<User> findByRole(Role role);
    
    Page<User> findByRole(Role role, Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE u.role IN :roles")
    List<User> findByRoles(@Param("roles") List<Role> roles);
    
    // Active/inactive users
    List<User> findByActiveTrue();
    
    List<User> findByActiveFalse();
    
    Page<User> findByActive(boolean active, Pageable pageable);
    
    // Email verification
    List<User> findByEmailVerified(boolean emailVerified);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.emailVerified = false AND u.createdAt < :cutoffDate")
    long countUnverifiedUsersOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    // Search queries
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "u.phoneNumber LIKE CONCAT('%', :search, '%')")
    Page<User> searchUsers(@Param("search") String search, Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE " +
           "(LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "u.phoneNumber LIKE CONCAT('%', :search, '%')) AND " +
           "u.role = :role")
    Page<User> searchUsersByRole(@Param("search") String search, @Param("role") Role role, Pageable pageable);
    
    // Activity queries
    @Query("SELECT u FROM User u WHERE u.lastLoginAt >= :since")
    List<User> findActiveUsersSince(@Param("since") LocalDateTime since);
    
    @Query("SELECT u FROM User u WHERE u.lastLoginAt < :before OR u.lastLoginAt IS NULL")
    List<User> findInactiveUsersBefore(@Param("before") LocalDateTime before);
    
    // Statistics queries
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    long countByRole(@Param("role") Role role);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :startDate AND u.createdAt <= :endDate")
    long countUsersCreatedBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.active = true")
    long countActiveUsers();
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.emailVerified = true")
    long countVerifiedUsers();
    
    // Bulk operations
    @Modifying
    @Query("UPDATE User u SET u.active = :active WHERE u.id IN :userIds")
    void updateActiveStatusByIds(@Param("userIds") List<Long> userIds, @Param("active") boolean active);
    
    @Modifying
    @Query("UPDATE User u SET u.emailVerified = true WHERE u.id = :userId")
    void markEmailAsVerified(@Param("userId") Long userId);
    
    @Modifying
    @Query("UPDATE User u SET u.phoneVerified = true WHERE u.id = :userId")
    void markPhoneAsVerified(@Param("userId") Long userId);
    
    @Modifying
    @Query("UPDATE User u SET u.lastLoginAt = :loginTime WHERE u.id = :userId")
    void updateLastLoginTime(@Param("userId") Long userId, @Param("loginTime") LocalDateTime loginTime);
    
    // Admin queries
    @Query("SELECT u FROM User u WHERE u.role IN ('ADMIN', 'SUPER_ADMIN')")
    List<User> findAllAdmins();
    
    @Query("SELECT u FROM User u WHERE u.role = 'DELIVERY_PERSON' AND u.active = true")
    List<User> findActiveDeliveryPersons();
    
    @Query("SELECT u FROM User u WHERE u.role = 'RESTAURANT_PARTNER' AND u.active = true")
    List<User> findActiveRestaurantPartners();
    
    // Advanced filters
    @Query("SELECT u FROM User u WHERE " +
           "(:role IS NULL OR u.role = :role) AND " +
           "(:active IS NULL OR u.active = :active) AND " +
           "(:emailVerified IS NULL OR u.emailVerified = :emailVerified) AND " +
           "(:createdAfter IS NULL OR u.createdAt >= :createdAfter) AND " +
           "(:createdBefore IS NULL OR u.createdAt <= :createdBefore)")
    Page<User> findUsersWithFilters(
        @Param("role") Role role,
        @Param("active") Boolean active,
        @Param("emailVerified") Boolean emailVerified,
        @Param("createdAfter") LocalDateTime createdAfter,
        @Param("createdBefore") LocalDateTime createdBefore,
        Pageable pageable
    );
}