package com.tiffin.newsletter.dto;

public class NewsletterSubscriptionResponse {
    
    private boolean success;
    private String message;
    private String email;
    
    // Constructors
    public NewsletterSubscriptionResponse() {}
    
    public NewsletterSubscriptionResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    public NewsletterSubscriptionResponse(boolean success, String message, String email) {
        this.success = success;
        this.message = message;
        this.email = email;
    }
    
    // Static factory methods
    public static NewsletterSubscriptionResponse success(String email) {
        return new NewsletterSubscriptionResponse(true, "Successfully subscribed to newsletter", email);
    }
    
    public static NewsletterSubscriptionResponse alreadySubscribed(String email) {
        return new NewsletterSubscriptionResponse(true, "You are already subscribed to our newsletter", email);
    }
    
    public static NewsletterSubscriptionResponse error(String message) {
        return new NewsletterSubscriptionResponse(false, message);
    }
    
    // Getters and Setters
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
}