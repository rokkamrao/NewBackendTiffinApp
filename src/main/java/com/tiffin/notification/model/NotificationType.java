package com.tiffin.notification.model;

/**
 * Notification types for different events
 */
public enum NotificationType {
    // Order lifecycle
    ORDER_PLACED("Order Placed", "Your order has been placed successfully"),
    ORDER_CONFIRMED("Order Confirmed", "Your order has been confirmed by the restaurant"),
    ORDER_PREPARING("Order Being Prepared", "Your order is being prepared"),
    ORDER_READY("Order Ready", "Your order is ready for pickup"),
    ORDER_PICKED_UP("Order Picked Up", "Your order has been picked up by delivery partner"),
    ORDER_OUT_FOR_DELIVERY("Out for Delivery", "Your order is out for delivery"),
    ORDER_DELIVERED("Order Delivered", "Your order has been delivered successfully"),
    ORDER_CANCELLED("Order Cancelled", "Your order has been cancelled"),
    
    // Payment events
    PAYMENT_SUCCESS("Payment Successful", "Your payment has been processed successfully"),
    PAYMENT_FAILED("Payment Failed", "Your payment could not be processed"),
    PAYMENT_REFUND("Refund Processed", "Your refund has been processed"),
    
    // Delivery updates
    DELIVERY_LOCATION_UPDATE("Delivery Update", "Your delivery partner location has been updated"),
    DELIVERY_DELAY("Delivery Delayed", "Your delivery is experiencing delays"),
    DELIVERY_PARTNER_ASSIGNED("Delivery Partner Assigned", "A delivery partner has been assigned to your order"),
    
    // Marketing and promotions
    PROMOTION("Special Offer", "Check out our latest offers and promotions"),
    DISCOUNT_AVAILABLE("Discount Available", "You have earned a discount on your next order"),
    
    // System notifications
    SYSTEM_ANNOUNCEMENT("System Announcement", "Important system update"),
    MAINTENANCE_SCHEDULED("Maintenance Scheduled", "System maintenance is scheduled"),
    
    // Subscription and loyalty
    SUBSCRIPTION_REMINDER("Subscription Reminder", "Your subscription needs attention"),
    SUBSCRIPTION_EXPIRED("Subscription Expired", "Your subscription has expired"),
    LOYALTY_POINTS_EARNED("Points Earned", "You have earned loyalty points"),
    LOYALTY_REWARD_AVAILABLE("Reward Available", "You can redeem a loyalty reward"),
    
    // Social and engagement
    REVIEW_REQUEST("Review Request", "Please share your experience with a review"),
    CHAT_MESSAGE("New Message", "You have received a new message"),
    FRIEND_RECOMMENDATION("Friend Recommendation", "A friend recommended a dish to you"),
    
    // Security and account
    SECURITY_ALERT("Security Alert", "Security notification for your account"),
    LOGIN_ALERT("Login Alert", "New login detected on your account"),
    PASSWORD_CHANGED("Password Changed", "Your password has been changed"),
    PROFILE_UPDATED("Profile Updated", "Your profile has been updated"),
    
    // Restaurant partner notifications
    NEW_ORDER_RESTAURANT("New Order", "You have received a new order"),
    ORDER_CANCELLED_RESTAURANT("Order Cancelled", "An order has been cancelled"),
    MENU_UPDATE_APPROVED("Menu Update Approved", "Your menu update has been approved"),
    
    // Delivery partner notifications
    DELIVERY_REQUEST("Delivery Request", "New delivery request available"),
    DELIVERY_COMPLETED("Delivery Completed", "Delivery has been completed successfully"),
    EARNINGS_UPDATE("Earnings Update", "Your earnings have been updated");
    
    private final String defaultTitle;
    private final String defaultMessage;
    
    NotificationType(String defaultTitle, String defaultMessage) {
        this.defaultTitle = defaultTitle;
        this.defaultMessage = defaultMessage;
    }
    
    public String getDefaultTitle() {
        return defaultTitle;
    }
    
    public String getDefaultMessage() {
        return defaultMessage;
    }
}