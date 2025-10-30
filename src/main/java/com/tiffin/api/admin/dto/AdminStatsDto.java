package com.tiffin.api.admin.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminStatsDto {
    private Long totalUsers;
    private Long activeSubscriptions;
    private Double todayRevenue;
    private Long pendingOrders;
    private Long deliveriesInProgress;
    private Long feedbackAlerts;
    private Long lowStockItems;
    private String systemHealth;
    private Long totalDishes;
}