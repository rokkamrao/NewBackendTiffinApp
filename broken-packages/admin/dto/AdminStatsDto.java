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
    
    // Manual builder method for compatibility
    public static AdminStatsDtoBuilder builder() {
        return new AdminStatsDtoBuilder();
    }
    
    public static class AdminStatsDtoBuilder {
        private Long totalUsers;
        private Long activeSubscriptions;
        private Double todayRevenue;
        private Long pendingOrders;
        private Long deliveriesInProgress;
        private Long feedbackAlerts;
        private Long lowStockItems;
        private String systemHealth;
        private Long totalDishes;
        
        public AdminStatsDtoBuilder totalUsers(Long totalUsers) {
            this.totalUsers = totalUsers;
            return this;
        }
        
        public AdminStatsDtoBuilder activeSubscriptions(Long activeSubscriptions) {
            this.activeSubscriptions = activeSubscriptions;
            return this;
        }
        
        public AdminStatsDtoBuilder todayRevenue(Double todayRevenue) {
            this.todayRevenue = todayRevenue;
            return this;
        }
        
        public AdminStatsDtoBuilder pendingOrders(Long pendingOrders) {
            this.pendingOrders = pendingOrders;
            return this;
        }
        
        public AdminStatsDtoBuilder deliveriesInProgress(Long deliveriesInProgress) {
            this.deliveriesInProgress = deliveriesInProgress;
            return this;
        }
        
        public AdminStatsDtoBuilder feedbackAlerts(Long feedbackAlerts) {
            this.feedbackAlerts = feedbackAlerts;
            return this;
        }
        
        public AdminStatsDtoBuilder lowStockItems(Long lowStockItems) {
            this.lowStockItems = lowStockItems;
            return this;
        }
        
        public AdminStatsDtoBuilder systemHealth(String systemHealth) {
            this.systemHealth = systemHealth;
            return this;
        }
        
        public AdminStatsDtoBuilder totalDishes(Long totalDishes) {
            this.totalDishes = totalDishes;
            return this;
        }
        
        public AdminStatsDto build() {
            AdminStatsDto dto = new AdminStatsDto();
            dto.totalUsers = this.totalUsers;
            dto.activeSubscriptions = this.activeSubscriptions;
            dto.todayRevenue = this.todayRevenue;
            dto.pendingOrders = this.pendingOrders;
            dto.deliveriesInProgress = this.deliveriesInProgress;
            dto.feedbackAlerts = this.feedbackAlerts;
            dto.lowStockItems = this.lowStockItems;
            dto.systemHealth = this.systemHealth;
            dto.totalDishes = this.totalDishes;
            return dto;
        }
    }
}