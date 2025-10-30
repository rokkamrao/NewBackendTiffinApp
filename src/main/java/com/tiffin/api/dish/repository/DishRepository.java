package com.tiffin.api.dish.repository;

import com.tiffin.api.dish.model.Dish;
import com.tiffin.api.dish.model.DietType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface DishRepository extends JpaRepository<Dish, Long> {
    List<Dish> findByType(DietType type);
    
    @Query("SELECT d FROM Dish d WHERE " +
           "(:query IS NULL OR LOWER(d.name) LIKE LOWER(CONCAT('%',:query,'%'))) AND " +
           "(:type IS NULL OR d.type = :type)")
    List<Dish> searchDishes(@Param("query") String query, @Param("type") DietType type);
    
    @Query("SELECT d FROM Dish d JOIN d.tags t WHERE t IN :tags")
    List<Dish> findByTags(@Param("tags") List<String> tags);

    // Additional helper used by service layer
    List<Dish> findByNameContainingIgnoreCaseAndCuisineInAndDietaryTagsIn(String name, List<String> cuisines, List<String> dietaryTags);
}