package com.tiffin.testimonial.controller;

import com.tiffin.testimonial.dto.TestimonialRequest;
import com.tiffin.testimonial.dto.TestimonialResponse;
import com.tiffin.testimonial.service.TestimonialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/testimonials")
@CrossOrigin(origins = "*")
public class TestimonialController {
    
    @Autowired
    private TestimonialService testimonialService;
    
    @GetMapping
    public ResponseEntity<List<TestimonialResponse>> getAllTestimonials() {
        List<TestimonialResponse> testimonials = testimonialService.getAllApprovedTestimonials();
        return ResponseEntity.ok(testimonials);
    }
    
    @GetMapping("/featured")
    public ResponseEntity<List<TestimonialResponse>> getFeaturedTestimonials() {
        List<TestimonialResponse> testimonials = testimonialService.getFeaturedTestimonials();
        return ResponseEntity.ok(testimonials);
    }
    
    @GetMapping("/approved")
    public ResponseEntity<List<TestimonialResponse>> getAllApprovedTestimonials() {
        List<TestimonialResponse> testimonials = testimonialService.getAllApprovedTestimonials();
        return ResponseEntity.ok(testimonials);
    }
    
    @GetMapping("/rating/{minRating}")
    public ResponseEntity<List<TestimonialResponse>> getTestimonialsByRating(@PathVariable Integer minRating) {
        List<TestimonialResponse> testimonials = testimonialService.getTestimonialsByMinRating(minRating);
        return ResponseEntity.ok(testimonials);
    }
    
    @PostMapping
    public ResponseEntity<TestimonialResponse> createTestimonial(
            @Valid @RequestBody TestimonialRequest request,
            @RequestParam(required = false) Long userId) {
        TestimonialResponse response = testimonialService.createTestimonial(request, userId);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{testimonialId}/approve")
    public ResponseEntity<Map<String, String>> approveTestimonial(@PathVariable Long testimonialId) {
        boolean success = testimonialService.approveTestimonial(testimonialId);
        
        Map<String, String> response = Map.of(
            "message", success ? "Testimonial approved successfully" : "Failed to approve testimonial",
            "status", success ? "success" : "error"
        );
        
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{testimonialId}/featured")
    public ResponseEntity<Map<String, String>> setFeatured(
            @PathVariable Long testimonialId,
            @RequestParam boolean featured) {
        boolean success = testimonialService.setFeatured(testimonialId, featured);
        
        Map<String, String> response = Map.of(
            "message", success ? "Featured status updated successfully" : "Failed to update featured status",
            "status", success ? "success" : "error"
        );
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getTestimonialStats() {
        Double averageRating = testimonialService.getAverageRating();
        long totalApproved = testimonialService.getApprovedTestimonialCount();
        
        Map<String, Object> stats = Map.of(
            "averageRating", averageRating != null ? averageRating : 0.0,
            "totalApprovedTestimonials", totalApproved,
            "status", "success"
        );
        
        return ResponseEntity.ok(stats);
    }
}