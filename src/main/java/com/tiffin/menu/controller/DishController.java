package com.tiffin.menu.controller;

import com.tiffin.menu.model.Dish;
import com.tiffin.menu.repository.DishRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/dishes")
@RequiredArgsConstructor
public class DishController {
    private final DishRepository dishRepository;
    
    @GetMapping
    public ResponseEntity<List<Dish>> getAllDishes(
            @RequestParam(required = false) Dish.Category category,
            @RequestParam(required = false, defaultValue = "false") boolean onlyAvailable) {
        
        if (category != null && onlyAvailable) {
            return ResponseEntity.ok(dishRepository.findByCategoryAndIsAvailableTrue(category));
        } else if (category != null) {
            return ResponseEntity.ok(dishRepository.findByCategory(category));
        } else if (onlyAvailable) {
            return ResponseEntity.ok(dishRepository.findByIsAvailableTrue());
        }
        
        return ResponseEntity.ok(dishRepository.findAll());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Dish> getDishById(@PathVariable Long id) {
        if (id == null) {
            return ResponseEntity.badRequest().build();
        }
        return dishRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/category/{category}")
    public ResponseEntity<List<Dish>> getDishesByCategory(@PathVariable Dish.Category category) {
        return ResponseEntity.ok(dishRepository.findByCategory(category));
    }
    
    @GetMapping("/available")
    public ResponseEntity<List<Dish>> getAvailableDishes() {
        return ResponseEntity.ok(dishRepository.findByIsAvailableTrue());
    }
    
    @GetMapping("/vegetarian")
    public ResponseEntity<List<Dish>> getVegetarianDishes() {
        return ResponseEntity.ok(dishRepository.findByIsVegetarianTrue());
    }
    
    @GetMapping("/bestsellers")
    public ResponseEntity<List<Dish>> getBestsellers() {
        return ResponseEntity.ok(dishRepository.findByIsBestsellerTrue());
    }
}