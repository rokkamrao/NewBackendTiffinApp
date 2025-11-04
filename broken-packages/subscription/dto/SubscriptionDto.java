package com.tiffin.api.subscription.dto;

import com.tiffin.api.subscription.model.SubscriptionPlan;
import com.tiffin.api.subscription.model.SubscriptionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionDto {
    private Long id;
    private Long userId;
    private SubscriptionPlan planType;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Integer mealsPerDay;
    private Integer mealsRemaining;
    private BigDecimal planPrice;
    private Boolean isActive;
    private SubscriptionStatus status;
    private LocalDateTime deliveryTime;
    private Long deliveryAddressId;
    private Boolean autoRenew;
    private LocalDateTime nextBillingDate;
}