package com.tiffin.membership.model;

/**
 * Loyalty tier enumeration with benefits
 */
public enum LoyaltyTier {
    BRONZE("Bronze", 0, 999, 1.0, "#CD7F32"),
    SILVER("Silver", 1000, 2999, 1.2, "#C0C0C0"),
    GOLD("Gold", 3000, 6999, 1.5, "#FFD700"),
    PLATINUM("Platinum", 7000, 14999, 2.0, "#E5E4E2"),
    DIAMOND("Diamond", 15000, Integer.MAX_VALUE, 2.5, "#B9F2FF");
    
    public static final int MIN_REDEMPTION_POINTS = 100;
    
    private final String displayName;
    private final int minPoints;
    private final int maxPoints;
    private final double pointsMultiplier;
    private final String color;
    
    LoyaltyTier(String displayName, int minPoints, int maxPoints, double pointsMultiplier, String color) {
        this.displayName = displayName;
        this.minPoints = minPoints;
        this.maxPoints = maxPoints;
        this.pointsMultiplier = pointsMultiplier;
        this.color = color;
    }
    
    public String getDisplayName() { return displayName; }
    public int getMinPoints() { return minPoints; }
    public int getMaxPoints() { return maxPoints; }
    public double getPointsMultiplier() { return pointsMultiplier; }
    public String getColor() { return color; }
    
    /**
     * Calculate tier based on lifetime points
     */
    public static LoyaltyTier calculateTier(int lifetimePoints) {
        for (LoyaltyTier tier : values()) {
            if (lifetimePoints >= tier.minPoints && lifetimePoints <= tier.maxPoints) {
                return tier;
            }
        }
        return BRONZE; // Default fallback
    }
    
    /**
     * Get points needed for next tier
     */
    public int getPointsForNextTier(int currentPoints) {
        if (this == DIAMOND) return 0; // Already at highest tier
        
        LoyaltyTier nextTier = getNextTier();
        if (nextTier != null) {
            return Math.max(0, nextTier.minPoints - currentPoints);
        }
        return 0;
    }
    
    /**
     * Get progress percentage to next tier
     */
    public double getProgressToNextTier(int currentPoints) {
        if (this == DIAMOND) return 100.0; // Already at highest tier
        
        LoyaltyTier nextTier = getNextTier();
        if (nextTier != null) {
            int progressPoints = currentPoints - this.minPoints;
            int tierRange = nextTier.minPoints - this.minPoints;
            return Math.min(100.0, (double) progressPoints / tierRange * 100.0);
        }
        return 100.0;
    }
    
    /**
     * Get next tier
     */
    private LoyaltyTier getNextTier() {
        LoyaltyTier[] tiers = values();
        for (int i = 0; i < tiers.length - 1; i++) {
            if (tiers[i] == this) {
                return tiers[i + 1];
            }
        }
        return null; // Already at highest tier
    }
}