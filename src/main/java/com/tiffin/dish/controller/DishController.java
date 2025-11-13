package com.tiffin.dish.controller;

import com.tiffin.dish.model.Dish;
import com.tiffin.dish.model.DietType;
import com.tiffin.dish.repository.DishRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/menu")
@RequiredArgsConstructor
public class DishController {
    private final DishRepository dishRepository;
    
    @GetMapping("/dishes")
    public ResponseEntity<List<Dish>> getAllDishes(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) DietType type) {
        if (query != null || type != null) {
            return ResponseEntity.ok(dishRepository.searchDishes(query, type));
        }
        return ResponseEntity.ok(dishRepository.findAll());
    }
    
    @GetMapping("/dishes/{id}")
    public ResponseEntity<Dish> getDishById(@PathVariable Long id) {
        return dishRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/dishes/by-type/{type}")
    public ResponseEntity<List<Dish>> getDishesByType(@PathVariable DietType type) {
        return ResponseEntity.ok(dishRepository.findByType(type));
    }
    
    @GetMapping("/dishes/by-tags")
    public ResponseEntity<List<Dish>> getDishesByTags(@RequestParam List<String> tags) {
        return ResponseEntity.ok(dishRepository.findByTags(tags));
    }
}