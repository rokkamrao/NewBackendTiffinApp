package com.tiffin.api.dish.service;

import com.tiffin.api.dish.dto.DishDto;
import com.tiffin.api.dish.model.Dish;
import com.tiffin.api.dish.repository.DishRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DishService {
    private final DishRepository dishRepository;

    @Transactional(readOnly = true)
    public Page<DishDto> getAllDishes(Pageable pageable) {
        return dishRepository.findAll(pageable)
                .map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public List<DishDto> searchDishes(String query, List<String> cuisines, List<String> dietaryTags) {
        return dishRepository.findByNameContainingIgnoreCaseAndCuisineInAndDietaryTagsIn(
                query, cuisines, dietaryTags)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DishDto getDishById(Long id) {
        return dishRepository.findById(id)
                .map(this::mapToDto)
                .orElseThrow(() -> new RuntimeException("Dish not found"));
    }

    @Transactional
    public DishDto createDish(DishDto dishDto) {
        Dish dish = mapToEntity(dishDto);
        Dish savedDish = dishRepository.save(dish);
        return mapToDto(savedDish);
    }

    @Transactional
    public DishDto updateDish(Long id, DishDto dishDto) {
        Dish existingDish = dishRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dish not found"));
        
        updateDishFromDto(existingDish, dishDto);
        Dish updatedDish = dishRepository.save(existingDish);
        return mapToDto(updatedDish);
    }

    @Transactional
    public void deleteDish(Long id) {
        dishRepository.deleteById(id);
    }

    private DishDto mapToDto(Dish dish) {
        return DishDto.builder()
                .id(dish.getId())
                .name(dish.getName())
                .description(dish.getDescription())
                .price(dish.getPrice())
                .imageUrl(dish.getImageUrl())
                .cuisine(dish.getCuisine())
                .dietaryTags(dish.getDietaryTags())
                .available(dish.isAvailable())
                .rating(dish.getRating())
                .reviewCount(dish.getReviewCount())
                .build();
    }

    private Dish mapToEntity(DishDto dto) {
        return Dish.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .imageUrl(dto.getImageUrl())
                .cuisine(dto.getCuisine())
                .dietaryTags(dto.getDietaryTags())
                .available(dto.isAvailable())
                .rating(dto.getRating())
                .reviewCount(dto.getReviewCount())
                .build();
    }

    private void updateDishFromDto(Dish dish, DishDto dto) {
        dish.setName(dto.getName());
        dish.setDescription(dto.getDescription());
        dish.setPrice(dto.getPrice());
        dish.setImageUrl(dto.getImageUrl());
        dish.setCuisine(dto.getCuisine());
        dish.setDietaryTags(dto.getDietaryTags());
        dish.setAvailable(dto.isAvailable());
        dish.setRating(dto.getRating());
        dish.setReviewCount(dto.getReviewCount());
    }
}