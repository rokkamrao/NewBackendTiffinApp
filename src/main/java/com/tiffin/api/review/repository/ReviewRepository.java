package com.tiffin.api.review.repository;

import com.tiffin.api.review.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    Page<Review> findByDishIdOrderByCreatedAtDesc(Long dishId, Pageable pageable);
    Page<Review> findByUserIdOrderByCreatedAtDesc(String userId, Pageable pageable);

    @Query("SELECT COALESCE(AVG(r.rating),0) FROM Review r WHERE r.dishId = :dishId")
    double getAverageRatingByDishId(@Param("dishId") Long dishId);

    int countByDishId(Long dishId);
}
