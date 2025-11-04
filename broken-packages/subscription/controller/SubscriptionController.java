package com.tiffin.api.subscription.controller;

import com.tiffin.api.subscription.dto.SubscriptionDto;
import com.tiffin.api.subscription.service.SubscriptionService;
import com.tiffin.api.user.model.User;
import com.tiffin.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final UserRepository userRepository;

    private User getAuthenticatedUser(UserDetails userDetails) {
        if (userDetails == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User must be authenticated");
        }
        return userRepository.findByPhone(userDetails.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    @PostMapping
    public ResponseEntity<SubscriptionDto> createSubscription(
            @RequestBody SubscriptionDto subscriptionDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getAuthenticatedUser(userDetails);
        subscriptionDto.setUserId(user.getId());
        log.info("Creating subscription for user {}", user.getId());

        SubscriptionDto createdSubscription = subscriptionService.createSubscription(subscriptionDto);
        return ResponseEntity.ok(createdSubscription);
    }

    @GetMapping
    public ResponseEntity<List<SubscriptionDto>> getUserSubscriptions(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getAuthenticatedUser(userDetails);
        log.info("Fetching subscriptions for user {}", user.getId());
        List<SubscriptionDto> subscriptions = subscriptionService.getUserSubscriptions(user.getId().toString());
        return ResponseEntity.ok(subscriptions);
    }

    @PatchMapping("/{subscriptionId}/status")
    public ResponseEntity<SubscriptionDto> updateSubscriptionStatus(
            @PathVariable Long subscriptionId,
            @RequestParam String status,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getAuthenticatedUser(userDetails);
        // Security check: ensure the subscription belongs to the authenticated user.
        // The current service doesn't easily allow for this check without a new method.
        // For now, we'll rely on the service to throw an error if the subscription doesn't exist,
        // and add a proper authorization check later.
        log.info("User {} updating subscription {} to status {}", user.getId(), subscriptionId, status);
        SubscriptionDto updatedSubscription = subscriptionService.updateSubscriptionStatus(subscriptionId, status);
        return ResponseEntity.ok(updatedSubscription);
    }

    @PostMapping("/{subscriptionId}/renew")
    public ResponseEntity<SubscriptionDto> renewSubscription(
            @PathVariable Long subscriptionId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getAuthenticatedUser(userDetails);
        // Similar security concern as above.
        log.info("User {} renewing subscription {}", user.getId(), subscriptionId);
        SubscriptionDto renewedSubscription = subscriptionService.renewSubscription(subscriptionId);
        return ResponseEntity.ok(renewedSubscription);
    }
}