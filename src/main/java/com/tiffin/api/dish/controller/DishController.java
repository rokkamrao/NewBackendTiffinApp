package com.tiffin.api.dish.controller;

import com.tiffin.api.dish.model.Dish;
import com.tiffin.api.dish.model.DietType;
import com.tiffin.api.dish.repository.DishRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/dishes")
@RequiredArgsConstructor
public class DishController {
    private final DishRepository dishRepository;
    
    @GetMapping
    public ResponseEntity<List<Dish>> getAllDishes(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) DietType type) {
        if (query != null || type != null) {
            return ResponseEntity.ok(dishRepository.searchDishes(query, type));
        }
        return ResponseEntity.ok(dishRepository.findAll());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Dish> getDishById(@PathVariable Long id) {
        return dishRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/by-type/{type}")
    public ResponseEntity<List<Dish>> getDishesByType(@PathVariable DietType type) {
        return ResponseEntity.ok(dishRepository.findByType(type));
    }
    
    @GetMapping("/by-tags")
    public ResponseEntity<List<Dish>> getDishesByTags(@RequestParam List<String> tags) {
        return ResponseEntity.ok(dishRepository.findByTags(tags));
    }
}