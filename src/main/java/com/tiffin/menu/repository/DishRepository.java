package com.tiffin.menu.repository;

import com.tiffin.menu.model.Dish;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DishRepository extends JpaRepository<Dish, Long> {
    List<Dish> findByIsAvailableTrue();
    
    List<Dish> findByCategory(Dish.Category category);
    
    List<Dish> findByCategoryAndIsAvailableTrue(Dish.Category category);
    
    List<Dish> findByIsVegetarianTrue();
    
    List<Dish> findByIsBestsellerTrue();
}
