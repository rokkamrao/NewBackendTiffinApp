package com.tiffin.testimonial.service;

import com.tiffin.testimonial.dto.TestimonialRequest;
import com.tiffin.testimonial.dto.TestimonialResponse;
import com.tiffin.testimonial.model.CustomerTestimonial;
import com.tiffin.testimonial.repository.CustomerTestimonialRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TestimonialService {
    
    private static final Logger logger = LoggerFactory.getLogger(TestimonialService.class);
    
    @Autowired
    private CustomerTestimonialRepository testimonialRepository;
    
    public List<TestimonialResponse> getFeaturedTestimonials() {
        List<CustomerTestimonial> testimonials = testimonialRepository.findByIsFeaturedTrueAndIsApprovedTrueOrderByDisplayOrderAscCreatedAtDesc();
        return testimonials.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    public List<TestimonialResponse> getAllApprovedTestimonials() {
        List<CustomerTestimonial> testimonials = testimonialRepository.findByIsApprovedTrueOrderByDisplayOrderAscCreatedAtDesc();
        return testimonials.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    public List<TestimonialResponse> getTestimonialsByMinRating(Integer minRating) {
        List<CustomerTestimonial> testimonials = testimonialRepository.findByMinimumRating(minRating);
        return testimonials.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    public TestimonialResponse createTestimonial(TestimonialRequest request, Long userId) {
        try {
            CustomerTestimonial testimonial = new CustomerTestimonial();
            testimonial.setCustomerName(request.getCustomerName());
            testimonial.setComment(request.getComment());
            testimonial.setRating(request.getRating());
            testimonial.setLocation(request.getLocation());
            testimonial.setAvatarEmoji(request.getAvatarEmoji());
            testimonial.setOrderId(request.getOrderId());
            testimonial.setUserId(userId);
            
            // New testimonials require approval
            testimonial.setIsApproved(false);
            testimonial.setIsFeatured(false);
            testimonial.setIsVerified(userId != null); // Verified if submitted by logged-in user
            
            CustomerTestimonial saved = testimonialRepository.save(testimonial);
            logger.info("New testimonial created with ID: {}", saved.getId());
            
            return convertToResponse(saved);
        } catch (Exception e) {
            logger.error("Error creating testimonial: ", e);
            throw new RuntimeException("Failed to create testimonial");
        }
    }
    
    public boolean approveTestimonial(Long testimonialId) {
        try {
            CustomerTestimonial testimonial = testimonialRepository.findById(testimonialId)
                    .orElseThrow(() -> new RuntimeException("Testimonial not found"));
            
            testimonial.setIsApproved(true);
            testimonial.setApprovedAt(LocalDateTime.now());
            testimonialRepository.save(testimonial);
            
            logger.info("Testimonial approved with ID: {}", testimonialId);
            return true;
        } catch (Exception e) {
            logger.error("Error approving testimonial: ", e);
            return false;
        }
    }
    
    public boolean setFeatured(Long testimonialId, boolean featured) {
        try {
            CustomerTestimonial testimonial = testimonialRepository.findById(testimonialId)
                    .orElseThrow(() -> new RuntimeException("Testimonial not found"));
            
            testimonial.setIsFeatured(featured);
            testimonialRepository.save(testimonial);
            
            logger.info("Testimonial featured status updated for ID: {}", testimonialId);
            return true;
        } catch (Exception e) {
            logger.error("Error updating featured status: ", e);
            return false;
        }
    }
    
    public Double getAverageRating() {
        return testimonialRepository.getAverageRating();
    }
    
    public long getApprovedTestimonialCount() {
        return testimonialRepository.countApproved();
    }
    
    private TestimonialResponse convertToResponse(CustomerTestimonial testimonial) {
        TestimonialResponse response = new TestimonialResponse();
        response.setId(testimonial.getId());
        response.setCustomerName(testimonial.getCustomerName());
        response.setComment(testimonial.getComment());
        response.setRating(testimonial.getRating());
        response.setLocation(testimonial.getLocation());
        response.setAvatarEmoji(testimonial.getAvatarEmoji());
        response.setCustomerImageUrl(testimonial.getCustomerImageUrl());
        response.setIsFeatured(testimonial.getIsFeatured());
        response.setIsVerified(testimonial.getIsVerified());
        response.setCreatedAt(testimonial.getCreatedAt());
        return response;
    }
}