package com.tiffin.newsletter.dto;

public class NewsletterResponse {
    
    private boolean success;
    private String message;
    private String email;
    private Long subscriberId;
    
    // Constructors
    public NewsletterResponse() {}
    
    public NewsletterResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    public NewsletterResponse(boolean success, String message, String email) {
        this.success = success;
        this.message = message;
        this.email = email;
    }
    
    // Static factory methods
    public static NewsletterResponse success(String email) {
        return new NewsletterResponse(true, "Operation successful", email);
    }
    
    public static NewsletterResponse error(String message) {
        return new NewsletterResponse(false, message);
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
    
    public Long getSubscriberId() {
        return subscriberId;
    }
    
    public void setSubscriberId(Long subscriberId) {
        this.subscriberId = subscriberId;
    }
}