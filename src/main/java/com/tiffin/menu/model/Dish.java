package com.tiffin.menu.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "dishes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Dish {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private double price;
    private String imageUrl;

    private boolean isVegetarian;
    private boolean isVegan;
    private boolean isGlutenFree;
    private boolean isJain;

    @Enumerated(EnumType.STRING)
    private Category category;

    private boolean isAvailable;
    private boolean isBestseller;

    // Nutrition Info
    private int calories;
    private double protein;
    private double carbs;
    private double fat;

    public enum Category {
        BREAKFAST, LUNCH, DINNER, SNACK
    }
}
