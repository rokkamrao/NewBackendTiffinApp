package com.tiffin.testimonial.repository;

import com.tiffin.testimonial.model.CustomerTestimonial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerTestimonialRepository extends JpaRepository<CustomerTestimonial, Long> {
    
    List<CustomerTestimonial> findByIsApprovedTrueOrderByDisplayOrderAscCreatedAtDesc();
    
    List<CustomerTestimonial> findByIsFeaturedTrueAndIsApprovedTrueOrderByDisplayOrderAscCreatedAtDesc();
    
    List<CustomerTestimonial> findByIsApprovedFalseOrderByCreatedAtDesc();
    
    @Query("SELECT t FROM CustomerTestimonial t WHERE t.isApproved = true AND t.rating >= ?1 ORDER BY t.displayOrder ASC, t.createdAt DESC")
    List<CustomerTestimonial> findByMinimumRating(Integer minRating);
    
    @Query("SELECT AVG(t.rating) FROM CustomerTestimonial t WHERE t.isApproved = true")
    Double getAverageRating();
    
    @Query("SELECT COUNT(t) FROM CustomerTestimonial t WHERE t.isApproved = true")
    long countApproved();
    
    List<CustomerTestimonial> findByUserIdAndIsApprovedTrue(Long userId);
}