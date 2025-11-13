package com.tiffin.landing.repository;

import com.tiffin.landing.model.FeaturedDish;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface FeaturedDishRepository extends JpaRepository<FeaturedDish, Long> {
    
    // Find featured dishes ordered by display order and creation date
    List<FeaturedDish> findByIsFeaturedTrueAndIsAvailableTrueOrderByDisplayOrderAscCreatedAtDesc();
    
    // Find popular dishes ordered by display order and creation date
    List<FeaturedDish> findByIsPopularTrueAndIsAvailableTrueOrderByDisplayOrderAscCreatedAtDesc();
    
    // Find dishes by category
    List<FeaturedDish> findByCategoryAndIsAvailableTrueOrderByDisplayOrderAscCreatedAtDesc(String category);
    
    // Find vegetarian dishes
    List<FeaturedDish> findByIsVegetarianTrueAndIsAvailableTrueOrderByDisplayOrderAscCreatedAtDesc();
    
    // Find vegan dishes
    List<FeaturedDish> findByIsVeganTrueAndIsAvailableTrueOrderByDisplayOrderAscCreatedAtDesc();
    
    // Find dishes by cuisine type
    List<FeaturedDish> findByCuisineTypeAndIsAvailableTrueOrderByDisplayOrderAscCreatedAtDesc(String cuisineType);
    
    // Find dishes by spice level
    List<FeaturedDish> findBySpiceLevelAndIsAvailableTrueOrderByDisplayOrderAscCreatedAtDesc(String spiceLevel);
    
    // Find dishes with minimum rating
    @Query("SELECT f FROM FeaturedDish f WHERE f.averageRating >= :minRating AND f.isAvailable = true ORDER BY f.averageRating DESC, f.displayOrder ASC")
    List<FeaturedDish> findByMinimumRating(@Param("minRating") BigDecimal minRating);
    
    // Find dishes within price range
    @Query("SELECT f FROM FeaturedDish f WHERE " +
           "((f.discountedPrice IS NOT NULL AND f.discountedPrice BETWEEN :minPrice AND :maxPrice) OR " +
           "(f.discountedPrice IS NULL AND f.price BETWEEN :minPrice AND :maxPrice)) AND " +
           "f.isAvailable = true ORDER BY f.displayOrder ASC, f.createdAt DESC")
    List<FeaturedDish> findByPriceRange(@Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice);
    
    // Find dishes with discounts
    @Query("SELECT f FROM FeaturedDish f WHERE f.discountedPrice IS NOT NULL AND f.discountedPrice < f.price AND f.isAvailable = true ORDER BY ((f.price - f.discountedPrice) / f.price) DESC")
    List<FeaturedDish> findDishesWithDiscounts();
    
    // Search dishes by name or description
    @Query("SELECT f FROM FeaturedDish f WHERE " +
           "(LOWER(f.dishName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(f.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(f.tags) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND " +
           "f.isAvailable = true ORDER BY f.displayOrder ASC, f.createdAt DESC")
    List<FeaturedDish> searchDishes(@Param("searchTerm") String searchTerm);
    
    // Find available dishes ordered by display order
    List<FeaturedDish> findByIsAvailableTrueOrderByDisplayOrderAscCreatedAtDesc();
    
    // Count featured dishes
    long countByIsFeaturedTrueAndIsAvailableTrue();
    
    // Count popular dishes
    long countByIsPopularTrueAndIsAvailableTrue();
    
    // Count available dishes
    long countByIsAvailableTrue();
    
    // Get average rating of all dishes
    @Query("SELECT AVG(f.averageRating) FROM FeaturedDish f WHERE f.averageRating IS NOT NULL AND f.isAvailable = true")
    BigDecimal getOverallAverageRating();
    
    // Get most expensive dish price
    @Query("SELECT MAX(CASE WHEN f.discountedPrice IS NOT NULL THEN f.discountedPrice ELSE f.price END) FROM FeaturedDish f WHERE f.isAvailable = true")
    BigDecimal getMaxPrice();
    
    // Get least expensive dish price
    @Query("SELECT MIN(CASE WHEN f.discountedPrice IS NOT NULL THEN f.discountedPrice ELSE f.price END) FROM FeaturedDish f WHERE f.isAvailable = true")
    BigDecimal getMinPrice();
    
    // Find dishes with quick preparation (less than specified minutes)
    List<FeaturedDish> findByPreparationTimeMinutesLessThanEqualAndIsAvailableTrueOrderByPreparationTimeMinutesAsc(Integer maxPreparationTime);
    
    // Find top rated dishes (limit implemented in service)
    @Query("SELECT f FROM FeaturedDish f WHERE f.averageRating IS NOT NULL AND f.isAvailable = true ORDER BY f.averageRating DESC, f.totalReviews DESC")
    List<FeaturedDish> findTopRatedDishes();
    
    // Find recently added dishes (limit implemented in service)
    @Query("SELECT f FROM FeaturedDish f WHERE f.isAvailable = true ORDER BY f.createdAt DESC")
    List<FeaturedDish> findRecentlyAddedDishes();
    
    // Find dishes by multiple categories
    @Query("SELECT f FROM FeaturedDish f WHERE f.category IN :categories AND f.isAvailable = true ORDER BY f.displayOrder ASC, f.createdAt DESC")
    List<FeaturedDish> findByMultipleCategories(@Param("categories") List<String> categories);
    
    // Find dishes by dietary preferences
    @Query("SELECT f FROM FeaturedDish f WHERE " +
           "(:includeVegetarian = false OR f.isVegetarian = :includeVegetarian) AND " +
           "(:includeVegan = false OR f.isVegan = :includeVegan) AND " +
           "(:includeGlutenFree = false OR f.isGlutenFree = :includeGlutenFree) AND " +
           "f.isAvailable = true ORDER BY f.displayOrder ASC, f.createdAt DESC")
    List<FeaturedDish> findByDietaryPreferences(
            @Param("includeVegetarian") boolean includeVegetarian,
            @Param("includeVegan") boolean includeVegan,
            @Param("includeGlutenFree") boolean includeGlutenFree
    );
}