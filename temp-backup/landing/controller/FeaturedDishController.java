package com.tiffin.landing.controller;

import com.tiffin.landing.dto.FeaturedDishDTOs.*;
import com.tiffin.landing.service.FeaturedDishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/featured-dishes")
@CrossOrigin(origins = "*")
public class FeaturedDishController {
    
    @Autowired
    private FeaturedDishService featuredDishService;
    
    @GetMapping("/featured")
    public ResponseEntity<List<FeaturedDishResponse>> getFeaturedDishes() {
        List<FeaturedDishResponse> dishes = featuredDishService.getFeaturedDishes();
        return ResponseEntity.ok(dishes);
    }
    
    @GetMapping("/featured/summary")
    public ResponseEntity<List<FeaturedDishSummary>> getFeaturedDishesSummary() {
        List<FeaturedDishSummary> dishes = featuredDishService.getFeaturedDishesSummary();
        return ResponseEntity.ok(dishes);
    }
    
    @GetMapping("/popular")
    public ResponseEntity<List<FeaturedDishResponse>> getPopularDishes() {
        List<FeaturedDishResponse> dishes = featuredDishService.getPopularDishes();
        return ResponseEntity.ok(dishes);
    }
    
    @GetMapping("/category/{category}")
    public ResponseEntity<List<FeaturedDishResponse>> getDishesByCategory(@PathVariable String category) {
        List<FeaturedDishResponse> dishes = featuredDishService.getDishesByCategory(category);
        return ResponseEntity.ok(dishes);
    }
    
    @GetMapping("/vegetarian")
    public ResponseEntity<List<FeaturedDishResponse>> getVegetarianDishes() {
        List<FeaturedDishResponse> dishes = featuredDishService.getVegetarianDishes();
        return ResponseEntity.ok(dishes);
    }
    
    @GetMapping("/vegan")
    public ResponseEntity<List<FeaturedDishResponse>> getVeganDishes() {
        List<FeaturedDishResponse> dishes = featuredDishService.getVeganDishes();
        return ResponseEntity.ok(dishes);
    }
    
    @GetMapping("/price-range")
    public ResponseEntity<List<FeaturedDishResponse>> getDishesByPriceRange(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice) {
        List<FeaturedDishResponse> dishes = featuredDishService.getDishesByPriceRange(minPrice, maxPrice);
        return ResponseEntity.ok(dishes);
    }
    
    @GetMapping("/discounts")
    public ResponseEntity<List<FeaturedDishResponse>> getDishesWithDiscounts() {
        List<FeaturedDishResponse> dishes = featuredDishService.getDishesWithDiscounts();
        return ResponseEntity.ok(dishes);
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<FeaturedDishResponse>> searchDishes(@RequestParam String q) {
        List<FeaturedDishResponse> dishes = featuredDishService.searchDishes(q);
        return ResponseEntity.ok(dishes);
    }
    
    @GetMapping("/top-rated")
    public ResponseEntity<List<FeaturedDishResponse>> getTopRatedDishes(
            @RequestParam(defaultValue = "10") int limit) {
        List<FeaturedDishResponse> dishes = featuredDishService.getTopRatedDishes(limit);
        return ResponseEntity.ok(dishes);
    }
    
    @GetMapping("/recent")
    public ResponseEntity<List<FeaturedDishResponse>> getRecentlyAddedDishes(
            @RequestParam(defaultValue = "10") int limit) {
        List<FeaturedDishResponse> dishes = featuredDishService.getRecentlyAddedDishes(limit);
        return ResponseEntity.ok(dishes);
    }
    
    @GetMapping("/quick-prep")
    public ResponseEntity<List<FeaturedDishResponse>> getQuickPreparationDishes(
            @RequestParam(defaultValue = "30") int maxMinutes) {
        List<FeaturedDishResponse> dishes = featuredDishService.getQuickPreparationDishes(maxMinutes);
        return ResponseEntity.ok(dishes);
    }
    
    @GetMapping("/{dishId}")
    public ResponseEntity<FeaturedDishResponse> getDishById(@PathVariable Long dishId) {
        Optional<FeaturedDishResponse> dish = featuredDishService.getDishById(dishId);
        return dish.map(ResponseEntity::ok)
                   .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<FeaturedDishResponse> createDish(@Valid @RequestBody FeaturedDishRequest request) {
        FeaturedDishResponse response = featuredDishService.createDish(request);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{dishId}")
    public ResponseEntity<FeaturedDishResponse> updateDish(
            @PathVariable Long dishId,
            @Valid @RequestBody FeaturedDishRequest request) {
        Optional<FeaturedDishResponse> response = featuredDishService.updateDish(dishId, request);
        return response.map(ResponseEntity::ok)
                       .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{dishId}")
    public ResponseEntity<Map<String, String>> deleteDish(@PathVariable Long dishId) {
        boolean success = featuredDishService.deleteDish(dishId);
        
        Map<String, String> response = Map.of(
            "message", success ? "Dish deleted successfully" : "Dish not found",
            "status", success ? "success" : "error"
        );
        
        return success ? ResponseEntity.ok(response) : ResponseEntity.notFound().build();
    }
    
    @PutMapping("/{dishId}/featured")
    public ResponseEntity<Map<String, String>> setFeaturedStatus(
            @PathVariable Long dishId,
            @RequestParam boolean featured) {
        boolean success = featuredDishService.setFeaturedStatus(dishId, featured);
        
        Map<String, String> response = Map.of(
            "message", success ? "Featured status updated successfully" : "Failed to update featured status",
            "status", success ? "success" : "error"
        );
        
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{dishId}/popular")
    public ResponseEntity<Map<String, String>> setPopularStatus(
            @PathVariable Long dishId,
            @RequestParam boolean popular) {
        boolean success = featuredDishService.setPopularStatus(dishId, popular);
        
        Map<String, String> response = Map.of(
            "message", success ? "Popular status updated successfully" : "Failed to update popular status",
            "status", success ? "success" : "error"
        );
        
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{dishId}/availability")
    public ResponseEntity<Map<String, String>> setAvailabilityStatus(
            @PathVariable Long dishId,
            @RequestParam boolean available) {
        boolean success = featuredDishService.setAvailabilityStatus(dishId, available);
        
        Map<String, String> response = Map.of(
            "message", success ? "Availability status updated successfully" : "Failed to update availability status",
            "status", success ? "success" : "error"
        );
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDishStats() {
        Map<String, Object> stats = Map.of(
            "featuredCount", featuredDishService.getFeaturedDishCount(),
            "popularCount", featuredDishService.getPopularDishCount(),
            "availableCount", featuredDishService.getAvailableDishCount(),
            "averageRating", featuredDishService.getOverallAverageRating(),
            "maxPrice", featuredDishService.getMaxPrice(),
            "minPrice", featuredDishService.getMinPrice(),
            "status", "success"
        );
        
        return ResponseEntity.ok(stats);
    }
}