package com.tiffin.admin.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * Admin Menu Management Controller
 * Handles admin endpoints for menu categories and management
 */
@RestController
@RequestMapping("/admin/menu")
@Slf4j
public class AdminMenuController {
    
    /**
     * Get menu categories for admin management
     */
    @GetMapping("/categories")
    public ResponseEntity<List<Map<String, Object>>> getMenuCategories() {
        log.debug("Fetching menu categories for admin");
        
        // For now, return a basic structure
        // This should be replaced with actual category entities from database
        List<Map<String, Object>> categories = List.of(
            Map.of(
                "id", "north-indian",
                "name", "North Indian",
                "description", "Traditional North Indian cuisine",
                "displayOrder", 1,
                "active", true,
                "status", "active",
                "itemCount", 25
            ),
            Map.of(
                "id", "south-indian", 
                "name", "South Indian",
                "description", "Authentic South Indian dishes",
                "displayOrder", 2,
                "active", true,
                "status", "active",
                "itemCount", 18
            ),
            Map.of(
                "id", "chinese",
                "name", "Chinese",
                "description", "Indo-Chinese fusion cuisine",
                "displayOrder", 3,
                "active", true,
                "status", "active",
                "itemCount", 12
            )
        );
        
        return ResponseEntity.ok(categories);
    }
}