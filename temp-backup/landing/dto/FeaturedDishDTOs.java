package com.tiffin.landing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.DecimalMax;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class FeaturedDishDTOs {
    
    // Request DTO for creating/updating featured dishes
    public static class FeaturedDishRequest {
        
        @NotBlank(message = "Dish name is required")
        private String dishName;
        
        private String description;
        private String shortDescription;
        
        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
        @DecimalMax(value = "9999.99", message = "Price must be less than 10000")
        private BigDecimal price;
        
        @DecimalMin(value = "0.0", inclusive = false, message = "Discounted price must be greater than 0")
        @DecimalMax(value = "9999.99", message = "Discounted price must be less than 10000")
        private BigDecimal discountedPrice;
        
        private String imageUrl;
        private String category;
        private String cuisineType;
        private Boolean isVegetarian = false;
        private Boolean isVegan = false;
        private Boolean isGlutenFree = false;
        private String spiceLevel;
        private Integer preparationTimeMinutes;
        private Integer caloriesPerServing;
        private Boolean isFeatured = false;
        private Boolean isPopular = false;
        private Boolean isAvailable = true;
        private Integer displayOrder = 0;
        private String tags;
        private String ingredients;
        private String nutritionalInfo;
        
        // Getters and Setters
        public String getDishName() { return dishName; }
        public void setDishName(String dishName) { this.dishName = dishName; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getShortDescription() { return shortDescription; }
        public void setShortDescription(String shortDescription) { this.shortDescription = shortDescription; }
        
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        
        public BigDecimal getDiscountedPrice() { return discountedPrice; }
        public void setDiscountedPrice(BigDecimal discountedPrice) { this.discountedPrice = discountedPrice; }
        
        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
        
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        
        public String getCuisineType() { return cuisineType; }
        public void setCuisineType(String cuisineType) { this.cuisineType = cuisineType; }
        
        public Boolean getIsVegetarian() { return isVegetarian; }
        public void setIsVegetarian(Boolean isVegetarian) { this.isVegetarian = isVegetarian; }
        
        public Boolean getIsVegan() { return isVegan; }
        public void setIsVegan(Boolean isVegan) { this.isVegan = isVegan; }
        
        public Boolean getIsGlutenFree() { return isGlutenFree; }
        public void setIsGlutenFree(Boolean isGlutenFree) { this.isGlutenFree = isGlutenFree; }
        
        public String getSpiceLevel() { return spiceLevel; }
        public void setSpiceLevel(String spiceLevel) { this.spiceLevel = spiceLevel; }
        
        public Integer getPreparationTimeMinutes() { return preparationTimeMinutes; }
        public void setPreparationTimeMinutes(Integer preparationTimeMinutes) { this.preparationTimeMinutes = preparationTimeMinutes; }
        
        public Integer getCaloriesPerServing() { return caloriesPerServing; }
        public void setCaloriesPerServing(Integer caloriesPerServing) { this.caloriesPerServing = caloriesPerServing; }
        
        public Boolean getIsFeatured() { return isFeatured; }
        public void setIsFeatured(Boolean isFeatured) { this.isFeatured = isFeatured; }
        
        public Boolean getIsPopular() { return isPopular; }
        public void setIsPopular(Boolean isPopular) { this.isPopular = isPopular; }
        
        public Boolean getIsAvailable() { return isAvailable; }
        public void setIsAvailable(Boolean isAvailable) { this.isAvailable = isAvailable; }
        
        public Integer getDisplayOrder() { return displayOrder; }
        public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
        
        public String getTags() { return tags; }
        public void setTags(String tags) { this.tags = tags; }
        
        public String getIngredients() { return ingredients; }
        public void setIngredients(String ingredients) { this.ingredients = ingredients; }
        
        public String getNutritionalInfo() { return nutritionalInfo; }
        public void setNutritionalInfo(String nutritionalInfo) { this.nutritionalInfo = nutritionalInfo; }
    }
    
    // Response DTO for featured dishes
    public static class FeaturedDishResponse {
        
        private Long id;
        private String dishName;
        private String description;
        private String shortDescription;
        private BigDecimal price;
        private BigDecimal discountedPrice;
        private String imageUrl;
        private String category;
        private String cuisineType;
        private Boolean isVegetarian;
        private Boolean isVegan;
        private Boolean isGlutenFree;
        private String spiceLevel;
        private BigDecimal averageRating;
        private Integer totalReviews;
        private Integer preparationTimeMinutes;
        private Integer caloriesPerServing;
        private Boolean isFeatured;
        private Boolean isPopular;
        private Boolean isAvailable;
        private Integer displayOrder;
        private String tags;
        private String ingredients;
        private String nutritionalInfo;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        
        // Computed fields
        private Boolean hasDiscount;
        private BigDecimal discountPercentage;
        private BigDecimal effectivePrice;
        
        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getDishName() { return dishName; }
        public void setDishName(String dishName) { this.dishName = dishName; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public String getShortDescription() { return shortDescription; }
        public void setShortDescription(String shortDescription) { this.shortDescription = shortDescription; }
        
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        
        public BigDecimal getDiscountedPrice() { return discountedPrice; }
        public void setDiscountedPrice(BigDecimal discountedPrice) { this.discountedPrice = discountedPrice; }
        
        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
        
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        
        public String getCuisineType() { return cuisineType; }
        public void setCuisineType(String cuisineType) { this.cuisineType = cuisineType; }
        
        public Boolean getIsVegetarian() { return isVegetarian; }
        public void setIsVegetarian(Boolean isVegetarian) { this.isVegetarian = isVegetarian; }
        
        public Boolean getIsVegan() { return isVegan; }
        public void setIsVegan(Boolean isVegan) { this.isVegan = isVegan; }
        
        public Boolean getIsGlutenFree() { return isGlutenFree; }
        public void setIsGlutenFree(Boolean isGlutenFree) { this.isGlutenFree = isGlutenFree; }
        
        public String getSpiceLevel() { return spiceLevel; }
        public void setSpiceLevel(String spiceLevel) { this.spiceLevel = spiceLevel; }
        
        public BigDecimal getAverageRating() { return averageRating; }
        public void setAverageRating(BigDecimal averageRating) { this.averageRating = averageRating; }
        
        public Integer getTotalReviews() { return totalReviews; }
        public void setTotalReviews(Integer totalReviews) { this.totalReviews = totalReviews; }
        
        public Integer getPreparationTimeMinutes() { return preparationTimeMinutes; }
        public void setPreparationTimeMinutes(Integer preparationTimeMinutes) { this.preparationTimeMinutes = preparationTimeMinutes; }
        
        public Integer getCaloriesPerServing() { return caloriesPerServing; }
        public void setCaloriesPerServing(Integer caloriesPerServing) { this.caloriesPerServing = caloriesPerServing; }
        
        public Boolean getIsFeatured() { return isFeatured; }
        public void setIsFeatured(Boolean isFeatured) { this.isFeatured = isFeatured; }
        
        public Boolean getIsPopular() { return isPopular; }
        public void setIsPopular(Boolean isPopular) { this.isPopular = isPopular; }
        
        public Boolean getIsAvailable() { return isAvailable; }
        public void setIsAvailable(Boolean isAvailable) { this.isAvailable = isAvailable; }
        
        public Integer getDisplayOrder() { return displayOrder; }
        public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
        
        public String getTags() { return tags; }
        public void setTags(String tags) { this.tags = tags; }
        
        public String getIngredients() { return ingredients; }
        public void setIngredients(String ingredients) { this.ingredients = ingredients; }
        
        public String getNutritionalInfo() { return nutritionalInfo; }
        public void setNutritionalInfo(String nutritionalInfo) { this.nutritionalInfo = nutritionalInfo; }
        
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        
        public LocalDateTime getUpdatedAt() { return updatedAt; }
        public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
        
        public Boolean getHasDiscount() { return hasDiscount; }
        public void setHasDiscount(Boolean hasDiscount) { this.hasDiscount = hasDiscount; }
        
        public BigDecimal getDiscountPercentage() { return discountPercentage; }
        public void setDiscountPercentage(BigDecimal discountPercentage) { this.discountPercentage = discountPercentage; }
        
        public BigDecimal getEffectivePrice() { return effectivePrice; }
        public void setEffectivePrice(BigDecimal effectivePrice) { this.effectivePrice = effectivePrice; }
    }
    
    // Simple response for listing
    public static class FeaturedDishSummary {
        private Long id;
        private String dishName;
        private String shortDescription;
        private BigDecimal price;
        private BigDecimal discountedPrice;
        private String imageUrl;
        private String category;
        private Boolean isVegetarian;
        private Boolean isVegan;
        private BigDecimal averageRating;
        private Integer preparationTimeMinutes;
        private Boolean hasDiscount;
        private BigDecimal effectivePrice;
        
        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getDishName() { return dishName; }
        public void setDishName(String dishName) { this.dishName = dishName; }
        
        public String getShortDescription() { return shortDescription; }
        public void setShortDescription(String shortDescription) { this.shortDescription = shortDescription; }
        
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        
        public BigDecimal getDiscountedPrice() { return discountedPrice; }
        public void setDiscountedPrice(BigDecimal discountedPrice) { this.discountedPrice = discountedPrice; }
        
        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
        
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        
        public Boolean getIsVegetarian() { return isVegetarian; }
        public void setIsVegetarian(Boolean isVegetarian) { this.isVegetarian = isVegetarian; }
        
        public Boolean getIsVegan() { return isVegan; }
        public void setIsVegan(Boolean isVegan) { this.isVegan = isVegan; }
        
        public BigDecimal getAverageRating() { return averageRating; }
        public void setAverageRating(BigDecimal averageRating) { this.averageRating = averageRating; }
        
        public Integer getPreparationTimeMinutes() { return preparationTimeMinutes; }
        public void setPreparationTimeMinutes(Integer preparationTimeMinutes) { this.preparationTimeMinutes = preparationTimeMinutes; }
        
        public Boolean getHasDiscount() { return hasDiscount; }
        public void setHasDiscount(Boolean hasDiscount) { this.hasDiscount = hasDiscount; }
        
        public BigDecimal getEffectivePrice() { return effectivePrice; }
        public void setEffectivePrice(BigDecimal effectivePrice) { this.effectivePrice = effectivePrice; }
    }
}