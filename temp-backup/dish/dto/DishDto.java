package com.tiffin.dish.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DishDto {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private String imageUrl;
    private String cuisine;
    private Set<String> dietaryTags;
    private boolean available;
    private double rating;
    private int reviewCount;
}

// package-private filter request to avoid multiple public types in one file
@Data
class DishFilterRequest {
    private String query;
    private String cuisine;
    private Set<String> dietaryTags;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Integer maxSpicyLevel;
}