package com.example.tiffinapi.notification.dto;

/**
 * DTO for notification data
 */
public class NotificationDto {
    
    private String title;
    private String message;
    private String type;
    private String data;

    public NotificationDto() {}

    public NotificationDto(String title, String message) {
        this.title = title;
        this.message = message;
        this.type = "info";
    }

    public NotificationDto(String title, String message, String type) {
        this.title = title;
        this.message = message;
        this.type = type;
    }

    public NotificationDto(String title, String message, String type, String data) {
        this.title = title;
        this.message = message;
        this.type = type;
        this.data = data;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}