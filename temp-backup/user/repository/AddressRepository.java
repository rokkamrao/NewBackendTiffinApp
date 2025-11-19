package com.tiffin.user.repository;

import com.tiffin.user.model.Address;
import com.tiffin.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    
    // Basic queries
    List<Address> findByUser(User user);
    
    List<Address> findByUserId(Long userId);
    
    Page<Address> findByUserId(Long userId, Pageable pageable);
    
    List<Address> findByUserAndActive(User user, boolean active);
    
    List<Address> findByUserIdAndActive(Long userId, boolean active);
    
    // Default address queries
    @Query("SELECT a FROM Address a WHERE a.user.id = :userId AND a.isDefault = true AND a.active = true")
    Optional<Address> findDefaultAddressByUserId(@Param("userId") Long userId);
    
    @Query("SELECT a FROM Address a WHERE a.user = :user AND a.isDefault = true AND a.active = true")
    Optional<Address> findDefaultAddressByUser(@Param("user") User user);
    
    boolean existsByUserAndIsDefaultAndActive(User user, boolean isDefault, boolean active);
    
    boolean existsByUserIdAndIsDefaultAndActive(Long userId, boolean isDefault, boolean active);
    
    // Address validation
    @Query("SELECT COUNT(a) FROM Address a WHERE a.user.id = :userId AND a.isDefault = true AND a.active = true")
    long countDefaultAddressesByUserId(@Param("userId") Long userId);
    
    // Geographic queries
    @Query("SELECT a FROM Address a WHERE a.latitude IS NOT NULL AND a.longitude IS NOT NULL")
    List<Address> findAddressesWithCoordinates();
    
    @Query("SELECT a FROM Address a WHERE a.latitude IS NULL OR a.longitude IS NULL")
    List<Address> findAddressesWithoutCoordinates();
    
    // Distance-based queries (using Haversine formula)
    @Query(value = "SELECT * FROM addresses a WHERE " +
           "a.latitude IS NOT NULL AND a.longitude IS NOT NULL AND " +
           "a.active = true AND " +
           "(6371 * acos(cos(radians(:lat)) * cos(radians(a.latitude)) * " +
           "cos(radians(a.longitude) - radians(:lng)) + " +
           "sin(radians(:lat)) * sin(radians(a.latitude)))) <= :radiusKm " +
           "ORDER BY (6371 * acos(cos(radians(:lat)) * cos(radians(a.latitude)) * " +
           "cos(radians(a.longitude) - radians(:lng)) + " +
           "sin(radians(:lat)) * sin(radians(a.latitude))))",
           nativeQuery = true)
    List<Address> findAddressesWithinRadius(
        @Param("lat") Double latitude,
        @Param("lng") Double longitude,
        @Param("radiusKm") Double radiusKm
    );
    
    // Search queries
    @Query("SELECT a FROM Address a WHERE a.user.id = :userId AND " +
           "(LOWER(a.label) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.street) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.city) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(a.state) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "a.zipCode LIKE CONCAT('%', :search, '%'))")
    List<Address> searchUserAddresses(@Param("userId") Long userId, @Param("search") String search);
    
    // City and area queries
    List<Address> findByCity(String city);
    
    List<Address> findByCityAndActive(String city, boolean active);
    
    List<Address> findByState(String state);
    
    List<Address> findByStateAndActive(String state, boolean active);
    
    List<Address> findByZipCode(String zipCode);
    
    @Query("SELECT DISTINCT a.city FROM Address a WHERE a.active = true ORDER BY a.city")
    List<String> findAllActiveCities();
    
    @Query("SELECT DISTINCT a.state FROM Address a WHERE a.active = true ORDER BY a.state")
    List<String> findAllActiveStates();
    
    @Query("SELECT DISTINCT a.zipCode FROM Address a WHERE a.city = :city AND a.active = true ORDER BY a.zipCode")
    List<String> findZipCodesByCity(@Param("city") String city);
    
    // Statistics queries
    @Query("SELECT COUNT(a) FROM Address a WHERE a.user.id = :userId AND a.active = true")
    long countActiveAddressesByUserId(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(a) FROM Address a WHERE a.city = :city AND a.active = true")
    long countActiveAddressesByCity(@Param("city") String city);
    
    @Query("SELECT a.city, COUNT(a) FROM Address a WHERE a.active = true GROUP BY a.city ORDER BY COUNT(a) DESC")
    List<Object[]> getAddressCountByCity();
    
    // Bulk operations
    @Modifying
    @Query("UPDATE Address a SET a.active = false WHERE a.user.id = :userId")
    void deactivateAllUserAddresses(@Param("userId") Long userId);
    
    @Modifying
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.user.id = :userId")
    void unmarkAllDefaultAddresses(@Param("userId") Long userId);
    
    @Modifying
    @Query("UPDATE Address a SET a.isDefault = false WHERE a.user.id = :userId AND a.id != :excludeAddressId")
    void unmarkOtherDefaultAddresses(@Param("userId") Long userId, @Param("excludeAddressId") Long excludeAddressId);
    
    @Modifying
    @Query("UPDATE Address a SET a.latitude = :latitude, a.longitude = :longitude WHERE a.id = :addressId")
    void updateCoordinates(@Param("addressId") Long addressId, @Param("latitude") Double latitude, @Param("longitude") Double longitude);
    
    // Delivery area queries
    @Query("SELECT DISTINCT a FROM Address a JOIN a.user u WHERE " +
           "a.city = :city AND a.active = true AND u.active = true")
    List<Address> findDeliverableAddressesInCity(@Param("city") String city);
    
    @Query("SELECT a FROM Address a WHERE " +
           "a.zipCode IN :zipCodes AND a.active = true")
    List<Address> findAddressesByZipCodes(@Param("zipCodes") List<String> zipCodes);
    
    // Validation queries
    @Query("SELECT a FROM Address a WHERE a.user.id = :userId AND " +
           "(a.street IS NULL OR a.street = '' OR " +
           "a.city IS NULL OR a.city = '' OR " +
           "a.state IS NULL OR a.state = '' OR " +
           "a.zipCode IS NULL OR a.zipCode = '')")
    List<Address> findIncompleteAddressesByUserId(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(a) FROM Address a WHERE " +
           "a.street IS NULL OR a.street = '' OR " +
           "a.city IS NULL OR a.city = '' OR " +
           "a.state IS NULL OR a.state = '' OR " +
           "a.zipCode IS NULL OR a.zipCode = ''")
    long countIncompleteAddresses();
}