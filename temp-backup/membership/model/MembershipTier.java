package com.tiffin.membership.model;

/**
 * Membership tier enumeration
 */
public enum MembershipTier {
    FREE("Free", 0),
    BASIC("Basic", 1),
    PREMIUM("Premium", 2),
    VIP("VIP", 3),
    ENTERPRISE("Enterprise", 4);
    
    private final String displayName;
    private final int level;
    
    MembershipTier(String displayName, int level) {
        this.displayName = displayName;
        this.level = level;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public int getLevel() {
        return level;
    }
    
    public boolean isHigherThan(MembershipTier other) {
        return this.level > other.level;
    }
}