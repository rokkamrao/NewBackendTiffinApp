package com.tiffin.landing.service;

import com.tiffin.landing.dto.FeaturedDishDTOs.*;
import com.tiffin.landing.model.FeaturedDish;
import com.tiffin.landing.repository.FeaturedDishRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class FeaturedDishService {
    
    private static final Logger logger = LoggerFactory.getLogger(FeaturedDishService.class);
    
    @Autowired
    private FeaturedDishRepository featuredDishRepository;
    
    public List<FeaturedDishResponse> getFeaturedDishes() {
        List<FeaturedDish> dishes = featuredDishRepository.findByIsFeaturedTrueAndIsAvailableTrueOrderByDisplayOrderAscCreatedAtDesc();
        return dishes.stream()
                .map(this::convertToFullResponse)
                .collect(Collectors.toList());
    }
    
    public List<FeaturedDishSummary> getFeaturedDishesSummary() {
        List<FeaturedDish> dishes = featuredDishRepository.findByIsFeaturedTrueAndIsAvailableTrueOrderByDisplayOrderAscCreatedAtDesc();
        return dishes.stream()
                .map(this::convertToSummary)
                .collect(Collectors.toList());
    }
    
    public List<FeaturedDishResponse> getPopularDishes() {
        List<FeaturedDish> dishes = featuredDishRepository.findByIsPopularTrueAndIsAvailableTrueOrderByDisplayOrderAscCreatedAtDesc();
        return dishes.stream()
                .map(this::convertToFullResponse)
                .collect(Collectors.toList());
    }
    
    public List<FeaturedDishResponse> getDishesByCategory(String category) {
        List<FeaturedDish> dishes = featuredDishRepository.findByCategoryAndIsAvailableTrueOrderByDisplayOrderAscCreatedAtDesc(category);
        return dishes.stream()
                .map(this::convertToFullResponse)
                .collect(Collectors.toList());
    }
    
    public List<FeaturedDishResponse> getVegetarianDishes() {
        List<FeaturedDish> dishes = featuredDishRepository.findByIsVegetarianTrueAndIsAvailableTrueOrderByDisplayOrderAscCreatedAtDesc();
        return dishes.stream()
                .map(this::convertToFullResponse)
                .collect(Collectors.toList());
    }
    
    public List<FeaturedDishResponse> getVeganDishes() {
        List<FeaturedDish> dishes = featuredDishRepository.findByIsVeganTrueAndIsAvailableTrueOrderByDisplayOrderAscCreatedAtDesc();
        return dishes.stream()
                .map(this::convertToFullResponse)
                .collect(Collectors.toList());
    }
    
    public List<FeaturedDishResponse> getDishesByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        List<FeaturedDish> dishes = featuredDishRepository.findByPriceRange(minPrice, maxPrice);
        return dishes.stream()
                .map(this::convertToFullResponse)
                .collect(Collectors.toList());
    }
    
    public List<FeaturedDishResponse> getDishesWithDiscounts() {
        List<FeaturedDish> dishes = featuredDishRepository.findDishesWithDiscounts();
        return dishes.stream()
                .map(this::convertToFullResponse)
                .collect(Collectors.toList());
    }
    
    public List<FeaturedDishResponse> searchDishes(String searchTerm) {
        List<FeaturedDish> dishes = featuredDishRepository.searchDishes(searchTerm);
        return dishes.stream()
                .map(this::convertToFullResponse)
                .collect(Collectors.toList());
    }
    
    public List<FeaturedDishResponse> getTopRatedDishes(int limit) {
        List<FeaturedDish> dishes = featuredDishRepository.findTopRatedDishes();
        return dishes.stream()
                .limit(limit)
                .map(this::convertToFullResponse)
                .collect(Collectors.toList());
    }
    
    public List<FeaturedDishResponse> getRecentlyAddedDishes(int limit) {
        List<FeaturedDish> dishes = featuredDishRepository.findRecentlyAddedDishes();
        return dishes.stream()
                .limit(limit)
                .map(this::convertToFullResponse)
                .collect(Collectors.toList());
    }
    
    public List<FeaturedDishResponse> getQuickPreparationDishes(int maxMinutes) {
        List<FeaturedDish> dishes = featuredDishRepository.findByPreparationTimeMinutesLessThanEqualAndIsAvailableTrueOrderByPreparationTimeMinutesAsc(maxMinutes);
        return dishes.stream()
                .map(this::convertToFullResponse)
                .collect(Collectors.toList());
    }
    
    public Optional<FeaturedDishResponse> getDishById(Long dishId) {
        return featuredDishRepository.findById(dishId)
                .map(this::convertToFullResponse);
    }
    
    public FeaturedDishResponse createDish(FeaturedDishRequest request) {
        try {
            FeaturedDish dish = new FeaturedDish();
            mapRequestToEntity(request, dish);
            
            FeaturedDish saved = featuredDishRepository.save(dish);
            logger.info("New featured dish created with ID: {}", saved.getId());
            
            return convertToFullResponse(saved);
        } catch (Exception e) {
            logger.error("Error creating featured dish: ", e);
            throw new RuntimeException("Failed to create featured dish");
        }
    }
    
    public Optional<FeaturedDishResponse> updateDish(Long dishId, FeaturedDishRequest request) {
        try {
            return featuredDishRepository.findById(dishId)
                    .map(dish -> {
                        mapRequestToEntity(request, dish);
                        FeaturedDish updated = featuredDishRepository.save(dish);
                        logger.info("Featured dish updated with ID: {}", dishId);
                        return convertToFullResponse(updated);
                    });
        } catch (Exception e) {
            logger.error("Error updating featured dish: ", e);
            throw new RuntimeException("Failed to update featured dish");
        }
    }
    
    public boolean deleteDish(Long dishId) {
        try {
            if (featuredDishRepository.existsById(dishId)) {
                featuredDishRepository.deleteById(dishId);
                logger.info("Featured dish deleted with ID: {}", dishId);
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("Error deleting featured dish: ", e);
            return false;
        }
    }
    
    public boolean setFeaturedStatus(Long dishId, boolean featured) {
        try {
            return featuredDishRepository.findById(dishId)
                    .map(dish -> {
                        dish.setIsFeatured(featured);
                        featuredDishRepository.save(dish);
                        logger.info("Featured status updated for dish ID: {}", dishId);
                        return true;
                    })
                    .orElse(false);
        } catch (Exception e) {
            logger.error("Error updating featured status: ", e);
            return false;
        }
    }
    
    public boolean setPopularStatus(Long dishId, boolean popular) {
        try {
            return featuredDishRepository.findById(dishId)
                    .map(dish -> {
                        dish.setIsPopular(popular);
                        featuredDishRepository.save(dish);
                        logger.info("Popular status updated for dish ID: {}", dishId);
                        return true;
                    })
                    .orElse(false);
        } catch (Exception e) {
            logger.error("Error updating popular status: ", e);
            return false;
        }
    }
    
    public boolean setAvailabilityStatus(Long dishId, boolean available) {
        try {
            return featuredDishRepository.findById(dishId)
                    .map(dish -> {
                        dish.setIsAvailable(available);
                        featuredDishRepository.save(dish);
                        logger.info("Availability status updated for dish ID: {}", dishId);
                        return true;
                    })
                    .orElse(false);
        } catch (Exception e) {
            logger.error("Error updating availability status: ", e);
            return false;
        }
    }
    
    // Statistics methods
    public long getFeaturedDishCount() {
        return featuredDishRepository.countByIsFeaturedTrueAndIsAvailableTrue();
    }
    
    public long getPopularDishCount() {
        return featuredDishRepository.countByIsPopularTrueAndIsAvailableTrue();
    }
    
    public long getAvailableDishCount() {
        return featuredDishRepository.countByIsAvailableTrue();
    }
    
    public BigDecimal getOverallAverageRating() {
        BigDecimal avg = featuredDishRepository.getOverallAverageRating();
        return avg != null ? avg : BigDecimal.ZERO;
    }
    
    public BigDecimal getMaxPrice() {
        BigDecimal max = featuredDishRepository.getMaxPrice();
        return max != null ? max : BigDecimal.ZERO;
    }
    
    public BigDecimal getMinPrice() {
        BigDecimal min = featuredDishRepository.getMinPrice();
        return min != null ? min : BigDecimal.ZERO;
    }
    
    // Helper methods
    private void mapRequestToEntity(FeaturedDishRequest request, FeaturedDish dish) {
        dish.setDishName(request.getDishName());
        dish.setDescription(request.getDescription());
        dish.setShortDescription(request.getShortDescription());
        dish.setPrice(request.getPrice());
        dish.setDiscountedPrice(request.getDiscountedPrice());
        dish.setImageUrl(request.getImageUrl());
        dish.setCategory(request.getCategory());
        dish.setCuisineType(request.getCuisineType());
        dish.setIsVegetarian(request.getIsVegetarian());
        dish.setIsVegan(request.getIsVegan());
        dish.setIsGlutenFree(request.getIsGlutenFree());
        dish.setSpiceLevel(request.getSpiceLevel());
        dish.setPreparationTimeMinutes(request.getPreparationTimeMinutes());
        dish.setCaloriesPerServing(request.getCaloriesPerServing());
        dish.setIsFeatured(request.getIsFeatured());
        dish.setIsPopular(request.getIsPopular());
        dish.setIsAvailable(request.getIsAvailable());
        dish.setDisplayOrder(request.getDisplayOrder());
        dish.setTags(request.getTags());
        dish.setIngredients(request.getIngredients());
        dish.setNutritionalInfo(request.getNutritionalInfo());
    }
    
    private FeaturedDishResponse convertToFullResponse(FeaturedDish dish) {
        FeaturedDishResponse response = new FeaturedDishResponse();
        response.setId(dish.getId());
        response.setDishName(dish.getDishName());
        response.setDescription(dish.getDescription());
        response.setShortDescription(dish.getShortDescription());
        response.setPrice(dish.getPrice());
        response.setDiscountedPrice(dish.getDiscountedPrice());
        response.setImageUrl(dish.getImageUrl());
        response.setCategory(dish.getCategory());
        response.setCuisineType(dish.getCuisineType());
        response.setIsVegetarian(dish.getIsVegetarian());
        response.setIsVegan(dish.getIsVegan());
        response.setIsGlutenFree(dish.getIsGlutenFree());
        response.setSpiceLevel(dish.getSpiceLevel());
        response.setAverageRating(dish.getAverageRating());
        response.setTotalReviews(dish.getTotalReviews());
        response.setPreparationTimeMinutes(dish.getPreparationTimeMinutes());
        response.setCaloriesPerServing(dish.getCaloriesPerServing());
        response.setIsFeatured(dish.getIsFeatured());
        response.setIsPopular(dish.getIsPopular());
        response.setIsAvailable(dish.getIsAvailable());
        response.setDisplayOrder(dish.getDisplayOrder());
        response.setTags(dish.getTags());
        response.setIngredients(dish.getIngredients());
        response.setNutritionalInfo(dish.getNutritionalInfo());
        response.setCreatedAt(dish.getCreatedAt());
        response.setUpdatedAt(dish.getUpdatedAt());
        
        // Set computed fields
        response.setHasDiscount(dish.hasDiscount());
        response.setDiscountPercentage(dish.getDiscountPercentage());
        response.setEffectivePrice(dish.getEffectivePrice());
        
        return response;
    }
    
    private FeaturedDishSummary convertToSummary(FeaturedDish dish) {
        FeaturedDishSummary summary = new FeaturedDishSummary();
        summary.setId(dish.getId());
        summary.setDishName(dish.getDishName());
        summary.setShortDescription(dish.getShortDescription());
        summary.setPrice(dish.getPrice());
        summary.setDiscountedPrice(dish.getDiscountedPrice());
        summary.setImageUrl(dish.getImageUrl());
        summary.setCategory(dish.getCategory());
        summary.setIsVegetarian(dish.getIsVegetarian());
        summary.setIsVegan(dish.getIsVegan());
        summary.setAverageRating(dish.getAverageRating());
        summary.setPreparationTimeMinutes(dish.getPreparationTimeMinutes());
        summary.setHasDiscount(dish.hasDiscount());
        summary.setEffectivePrice(dish.getEffectivePrice());
        
        return summary;
    }
}