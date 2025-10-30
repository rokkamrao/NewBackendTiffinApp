package com.tiffin.api.review.service;

import com.tiffin.api.dish.model.Dish;
import com.tiffin.api.dish.repository.DishRepository;
import com.tiffin.api.order.repository.OrderRepository;
import com.tiffin.api.review.dto.ReviewDto;
import com.tiffin.api.review.model.Review;
import com.tiffin.api.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final DishRepository dishRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public ReviewDto createReview(ReviewDto reviewDto) {
        // Verify if user has ordered this dish
    boolean hasOrdered = orderRepository.existsByUser_IdAndItems_Dish_Id(
        Long.parseLong(reviewDto.getUserId()), reviewDto.getDishId());
        
        Review review = Review.builder()
                .dishId(reviewDto.getDishId())
                .userId(reviewDto.getUserId())
                .userName(reviewDto.getUserName())
                .rating(reviewDto.getRating())
                .comment(reviewDto.getComment())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isVerifiedOrder(hasOrdered)
                .build();

        Review savedReview = reviewRepository.save(review);
        
        // Update dish rating
        updateDishRating(reviewDto.getDishId());
        
        return mapToDto(savedReview);
    }

    @Transactional(readOnly = true)
    public Page<ReviewDto> getDishReviews(Long dishId, Pageable pageable) {
        return reviewRepository.findByDishIdOrderByCreatedAtDesc(dishId, pageable)
                .map(this::mapToDto);
    }

    @Transactional(readOnly = true)
    public Page<ReviewDto> getUserReviews(String userId, Pageable pageable) {
        return reviewRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::mapToDto);
    }

    @Transactional
    public ReviewDto updateReview(Long reviewId, ReviewDto reviewDto) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        
        if (!review.getUserId().equals(reviewDto.getUserId())) {
            throw new RuntimeException("Unauthorized to update this review");
        }

        review.setRating(reviewDto.getRating());
        review.setComment(reviewDto.getComment());
        review.setUpdatedAt(LocalDateTime.now());

        Review updatedReview = reviewRepository.save(review);
        
        // Update dish rating
        updateDishRating(review.getDishId());
        
        return mapToDto(updatedReview);
    }

    @Transactional
    public void deleteReview(Long reviewId, String userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        
        if (!review.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized to delete this review");
        }

        reviewRepository.delete(review);
        
        // Update dish rating
        updateDishRating(review.getDishId());
    }

    private void updateDishRating(Long dishId) {
        Dish dish = dishRepository.findById(dishId)
                .orElseThrow(() -> new RuntimeException("Dish not found"));
        
        double avgRating = reviewRepository.getAverageRatingByDishId(dishId);
        int reviewCount = reviewRepository.countByDishId(dishId);
        
        dish.setRating(avgRating);
        dish.setReviewCount(reviewCount);
        dishRepository.save(dish);
    }

    private ReviewDto mapToDto(Review review) {
        return ReviewDto.builder()
                .id(review.getId())
                .dishId(review.getDishId())
                .userId(review.getUserId())
                .userName(review.getUserName())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .isVerifiedOrder(review.isVerifiedOrder())
                .build();
    }
}