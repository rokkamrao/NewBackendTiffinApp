package com.tiffin.common.enums;

/**
 * Enum defining different recurrence patterns for scheduled orders
 * Supports standard calendar recurrence patterns with custom intervals
 */
public enum RecurrencePattern {
    ONCE("Once", "One-time order"),
    DAILY("Daily", "Repeat every day"),
    WEEKLY("Weekly", "Repeat every week"),
    BIWEEKLY("Bi-weekly", "Repeat every two weeks"),
    MONTHLY("Monthly", "Repeat every month"),
    CUSTOM("Custom", "Custom recurrence pattern");

    private final String displayName;
    private final String description;

    RecurrencePattern(String displayName, String description) {
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
     * Check if this pattern requires custom interval specification
     */
    public boolean requiresCustomInterval() {
        return this == CUSTOM;
    }

    /**
     * Get default interval in days for standard patterns
     */
    public int getDefaultIntervalDays() {
        return switch (this) {
            case ONCE -> 0;
            case DAILY -> 1;
            case WEEKLY -> 7;
            case BIWEEKLY -> 14;
            case MONTHLY -> 30; // Approximate, will be calculated precisely
            case CUSTOM -> 0; // Requires custom specification
        };
    }
}