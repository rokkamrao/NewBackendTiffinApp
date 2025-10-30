package com.tiffin.api.subscription.service;

import com.tiffin.api.subscription.dto.SubscriptionDto;
import com.tiffin.api.subscription.model.Subscription;
import com.tiffin.api.subscription.model.SubscriptionPlan;
import com.tiffin.api.subscription.model.SubscriptionStatus;
import com.tiffin.api.subscription.repository.SubscriptionRepository;
import com.tiffin.api.user.model.Address;
import com.tiffin.api.user.model.User;
import com.tiffin.api.user.repository.UserRepository;
import com.tiffin.api.user.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;

    @Transactional
    public SubscriptionDto createSubscription(SubscriptionDto dto) {
        Subscription subscription = mapToEntity(dto);
        if (subscription.getStatus() == null) {
            subscription.setStatus(SubscriptionStatus.ACTIVE);
        }
        Subscription saved = subscriptionRepository.save(subscription);
        return mapToDto(saved);
    }

    @Transactional(readOnly = true)
    public List<SubscriptionDto> getUserSubscriptions(String userId) {
        User user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new RuntimeException("User not found"));
        return subscriptionRepository.findByUserOrderByStartDateDesc(user).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public SubscriptionDto updateSubscriptionStatus(Long id, String status) {
        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));
        SubscriptionStatus newStatus = SubscriptionStatus.valueOf(status);
        subscription.setStatus(newStatus);
        if (newStatus == SubscriptionStatus.PAUSED) {
            subscription.setEndDate(subscription.getEndDate().plusDays(7));
        }
        Subscription updated = subscriptionRepository.save(subscription);
        return mapToDto(updated);
    }

    @Transactional
    public SubscriptionDto renewSubscription(Long id) {
        Subscription subscription = subscriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        LocalDateTime currentEnd = subscription.getEndDate();
        LocalDateTime newEnd = switch (subscription.getPlanType()) {
            case DAILY -> currentEnd.plusDays(1);
            case WEEKLY -> currentEnd.plusWeeks(1);
            case MONTHLY -> currentEnd.plusMonths(1);
        };

        subscription.setEndDate(newEnd);
        subscription.setNextBillingDate(newEnd);
        Subscription renewed = subscriptionRepository.save(subscription);
        return mapToDto(renewed);
    }

    private SubscriptionDto mapToDto(Subscription s) {
        return SubscriptionDto.builder()
                .id(s.getId())
                .userId(s.getUser() != null ? s.getUser().getId() : null)
                .planType(s.getPlanType())
                .startDate(s.getStartDate())
                .endDate(s.getEndDate())
                .mealsPerDay(s.getMealsPerDay())
                .mealsRemaining(s.getMealsRemaining())
                .planPrice(s.getPlanPrice())
                .isActive(s.getIsActive())
                .status(s.getStatus())
                .deliveryTime(s.getDeliveryTime())
                .deliveryAddressId(s.getDeliveryAddress() != null ? s.getDeliveryAddress().getId() : null)
                .autoRenew(s.getAutoRenew())
                .nextBillingDate(s.getNextBillingDate())
                .build();
    }

    private Subscription mapToEntity(SubscriptionDto dto) {
        User user = null;
        if (dto.getUserId() != null) {
            user = userRepository.findById(dto.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
        }
        Address address = null;
        if (dto.getDeliveryAddressId() != null) {
            address = addressRepository.findById(dto.getDeliveryAddressId())
                    .orElseThrow(() -> new RuntimeException("Address not found"));
        }

        return Subscription.builder()
                .user(user)
                .planType(dto.getPlanType() != null ? dto.getPlanType() : SubscriptionPlan.MONTHLY)
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .mealsPerDay(dto.getMealsPerDay())
                .mealsRemaining(dto.getMealsRemaining())
                .planPrice(dto.getPlanPrice())
                .deliveryTime(dto.getDeliveryTime())
                .deliveryAddress(address)
                .autoRenew(dto.getAutoRenew() != null ? dto.getAutoRenew() : Boolean.FALSE)
                .nextBillingDate(dto.getNextBillingDate())
                .status(dto.getStatus())
                .build();
    }
}