package com.tiffin.common.enums;

/**
 * Enum for scheduled order status tracking
 * Manages lifecycle of recurring and one-time scheduled orders
 */
public enum ScheduledOrderStatus {
    ACTIVE("Active", "Order is active and will be executed as scheduled"),
    PAUSED("Paused", "Order is temporarily paused by user"),
    COMPLETED("Completed", "One-time order completed or recurring order ended"),
    CANCELLED("Cancelled", "Order was cancelled by user"),
    FAILED("Failed", "Order execution failed due to payment or other issues"),
    EXPIRED("Expired", "Order expired due to end date or max executions reached");

    private final String displayName;
    private final String description;

    ScheduledOrderStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Check if order can be executed
     */
    public boolean canExecute() {
        return this == ACTIVE;
    }

    /**
     * Check if order can be modified
     */
    public boolean canModify() {
        return this == ACTIVE || this == PAUSED;
    }

    /**
     * Get terminal statuses (orders that cannot be reactivated)
     */
    public static ScheduledOrderStatus[] getTerminalStatuses() {
        return new ScheduledOrderStatus[]{COMPLETED, CANCELLED, EXPIRED};
    }
}