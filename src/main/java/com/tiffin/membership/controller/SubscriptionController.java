package com.tiffin.membership.controller;

import com.tiffin.membership.model.MembershipPlan;
import com.tiffin.membership.service.MembershipService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Subscription Plans API Controller
 * Provides alias endpoints for frontend compatibility
 */
@RestController
@RequestMapping("/subscription-plans")
@RequiredArgsConstructor
@Slf4j
public class SubscriptionController {
    
    private final MembershipService membershipService;
    
    /**
     * Get all subscription plans (alias for membership plans)
     */
    @GetMapping
    public ResponseEntity<List<MembershipPlan>> getAllSubscriptionPlans() {
        log.debug("Fetching all subscription plans via alias endpoint");
        List<MembershipPlan> plans = membershipService.getActivePlans();
        return ResponseEntity.ok(plans);
    }
    
    /**
     * Get featured subscription plans
     */
    @GetMapping("/featured")
    public ResponseEntity<List<MembershipPlan>> getFeaturedSubscriptionPlans() {
        log.debug("Fetching featured subscription plans via alias endpoint");
        List<MembershipPlan> plans = membershipService.getFeaturedPlans();
        return ResponseEntity.ok(plans);
    }
}