package com.tiffin.notification.model;

/**
 * Notification priority levels
 */
public enum NotificationPriority {
    LOW("Low", 1, "#6B7280"),
    NORMAL("Normal", 2, "#3B82F6"),
    HIGH("High", 3, "#F59E0B"),
    URGENT("Urgent", 4, "#EF4444");
    
    private final String displayName;
    private final int level;
    private final String colorCode;
    
    NotificationPriority(String displayName, int level, String colorCode) {
        this.displayName = displayName;
        this.level = level;
        this.colorCode = colorCode;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public int getLevel() {
        return level;
    }
    
    public String getColorCode() {
        return colorCode;
    }
    
    /**
     * Check if this priority is higher than another
     */
    public boolean isHigherThan(NotificationPriority other) {
        return this.level > other.level;
    }
    
    /**
     * Check if this priority requires immediate attention
     */
    public boolean requiresImmediateAttention() {
        return this == HIGH || this == URGENT;
    }
}