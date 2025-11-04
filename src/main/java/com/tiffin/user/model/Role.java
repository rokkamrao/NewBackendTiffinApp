package com.tiffin.user.model;

/**
 * User roles in the system
 */
public enum Role {
    USER("Regular Customer"),
    PREMIUM_USER("Premium Customer"),
    DELIVERY_PERSON("Delivery Person"),
    RESTAURANT_PARTNER("Restaurant Partner"),
    ADMIN("System Administrator"),
    SUPER_ADMIN("Super Administrator");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isCustomer() {
        return this == USER || this == PREMIUM_USER;
    }

    public boolean isAdmin() {
        return this == ADMIN || this == SUPER_ADMIN;
    }

    public boolean canManageUsers() {
        return isAdmin();
    }

    public boolean canManageOrders() {
        return isAdmin() || this == DELIVERY_PERSON;
    }

    public boolean canManageRestaurants() {
        return isAdmin() || this == RESTAURANT_PARTNER;
    }

    public boolean canAccessAdminPanel() {
        return isAdmin();
    }

    public boolean canDeliverOrders() {
        return this == DELIVERY_PERSON;
    }

    public boolean hasPremiumFeatures() {
        return this == PREMIUM_USER || isAdmin();
    }
}