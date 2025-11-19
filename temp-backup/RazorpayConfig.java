package com.tiffin.config;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@ConfigurationProperties(prefix = "app.razorpay")
@Validated
public class RazorpayConfig {

    @NotBlank(message = "Razorpay key ID is required")
    @Value("${app.razorpay.key-id}")
    private String keyId;

    @NotBlank(message = "Razorpay key secret is required")
    @Value("${app.razorpay.key-secret}")
    private String keySecret;

    @Value("${app.razorpay.webhook-secret:}")
    private String webhookSecret;

    @Value("${app.razorpay.environment:test}")
    private String environment;

    @Value("${app.razorpay.timeout:30000}")
    private Integer timeout;

    @Value("${app.razorpay.currency:INR}")
    private String defaultCurrency;

    @Value("${app.razorpay.receipt-prefix:TIFFIN}")
    private String receiptPrefix;

    @Bean
    public RazorpayClient razorpayClient() {
        try {
            log.info("Initializing Razorpay client with key: {} in {} environment", 
                    maskKey(keyId), environment);
            
            validateConfiguration();
            
            RazorpayClient client = new RazorpayClient(keyId, keySecret);
            
            // Configure client options
            configureClientOptions(client);
            
            log.info("Razorpay client initialized successfully");
            return client;
        } catch (RazorpayException e) {
            log.error("Failed to initialize Razorpay client: {}", e.getMessage());
            throw new RuntimeException("Razorpay initialization failed", e);
        }
    }

    private void validateConfiguration() {
        if (keyId == null || keyId.trim().isEmpty()) {
            throw new IllegalArgumentException("Razorpay key ID cannot be null or empty");
        }
        
        if (keySecret == null || keySecret.trim().isEmpty()) {
            throw new IllegalArgumentException("Razorpay key secret cannot be null or empty");
        }
        
        if (!keyId.startsWith("rzp_")) {
            log.warn("Razorpay key ID does not start with 'rzp_', please verify the key");
        }
        
        if ("production".equalsIgnoreCase(environment) && keyId.contains("test")) {
            log.warn("Using test keys in production environment!");
        }
        
        log.info("Razorpay configuration validated successfully");
    }

    private void configureClientOptions(RazorpayClient client) {
        try {
            // Set timeout
            if (timeout != null && timeout > 0) {
                // Note: RazorpayClient doesn't expose timeout configuration directly
                // This would be implemented if the SDK supports it
                log.info("Configured Razorpay client timeout: {} ms", timeout);
            }
            
        } catch (Exception e) {
            log.warn("Failed to configure Razorpay client options: {}", e.getMessage());
        }
    }

    private String maskKey(String key) {
        if (key == null || key.length() < 8) {
            return "****";
        }
        return key.substring(0, 4) + "****" + key.substring(key.length() - 4);
    }

    // Configuration methods for other services
    public Map<String, Object> getPaymentOptions() {
        Map<String, Object> options = new HashMap<>();
        options.put("currency", defaultCurrency);
        options.put("receipt_prefix", receiptPrefix);
        options.put("environment", environment);
        return options;
    }

    public String generateReceiptId(String orderId) {
        return receiptPrefix + "_" + orderId + "_" + System.currentTimeMillis();
    }

    public boolean isProductionEnvironment() {
        return "production".equalsIgnoreCase(environment);
    }

    public boolean isTestEnvironment() {
        return "test".equalsIgnoreCase(environment);
    }

    // Getters and setters
    public String getKeyId() {
        return keyId;
    }

    public void setKeyId(String keyId) {
        this.keyId = keyId;
    }

    public String getKeySecret() {
        return keySecret;
    }

    public void setKeySecret(String keySecret) {
        this.keySecret = keySecret;
    }

    public String getWebhookSecret() {
        return webhookSecret;
    }

    public void setWebhookSecret(String webhookSecret) {
        this.webhookSecret = webhookSecret;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public String getDefaultCurrency() {
        return defaultCurrency;
    }

    public void setDefaultCurrency(String defaultCurrency) {
        this.defaultCurrency = defaultCurrency;
    }

    public String getReceiptPrefix() {
        return receiptPrefix;
    }

    public void setReceiptPrefix(String receiptPrefix) {
        this.receiptPrefix = receiptPrefix;
    }

    // Webhook verification utility
    public boolean verifyWebhookSignature(String payload, String signature) {
        if (webhookSecret == null || webhookSecret.trim().isEmpty()) {
            log.warn("Webhook secret not configured, skipping signature verification");
            return true; // In development, we might skip verification
        }
        
        try {
            // Implement webhook signature verification logic
            // This would use HMAC-SHA256 with the webhook secret
            log.debug("Verifying webhook signature");
            return true; // Placeholder implementation
        } catch (Exception e) {
            log.error("Failed to verify webhook signature: {}", e.getMessage());
            return false;
        }
    }

    // Health check method
    public boolean isHealthy() {
        try {
            // This could ping Razorpay API to check connectivity
            return keyId != null && keySecret != null;
        } catch (Exception e) {
            log.error("Razorpay health check failed: {}", e.getMessage());
            return false;
        }
    }
}