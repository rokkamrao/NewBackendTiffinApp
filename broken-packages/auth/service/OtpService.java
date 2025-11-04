package com.tiffin.api.auth.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class OtpService {
    
    private static class OtpData {
        String otp;
        LocalDateTime expiryTime;
        
        OtpData(String otp, LocalDateTime expiryTime) {
            this.otp = otp;
            this.expiryTime = expiryTime;
        }
    }
    
    // In-memory storage for OTPs (in production, use Redis or database)
    private final Map<String, OtpData> otpStorage = new ConcurrentHashMap<>();
    private final Random random = new Random();
    
    // OTP valid for 5 minutes
    private static final int OTP_VALIDITY_MINUTES = 5;
    
    // Developer mode - set to true to always use "123456" as OTP
    private static final boolean DEV_MODE = true;
    private static final String DEV_OTP = "123456";
    
    /**
     * Generate and store OTP for a phone number
     */
    public String generateOtp(String phone) {
        // In developer mode, always use fixed OTP
        String otp = DEV_MODE ? DEV_OTP : String.format("%06d", random.nextInt(999999));
        
        // Store with expiry time
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(OTP_VALIDITY_MINUTES);
        otpStorage.put(phone, new OtpData(otp, expiryTime));
        
        if (DEV_MODE) {
            log.info("[OtpService] 🔧 DEV MODE: Generated fixed OTP for {}: {}", phone, otp);
        } else {
            log.info("[OtpService] Generated OTP for {}: {} (expires at {})", phone, otp, expiryTime);
        }
        
        // In production, send SMS here
        // For development, just log it
        log.info("📱 SMS: Your OTP for password reset is: {}", otp);
        
        return otp;
    }
    
    /**
     * Verify OTP for a phone number
     */
    public boolean verifyOtp(String phone, String otp) {
        OtpData otpData = otpStorage.get(phone);
        
        if (otpData == null) {
            log.warn("[OtpService] No OTP found for phone: {}", phone);
            return false;
        }
        
        // Check if OTP is expired
        if (LocalDateTime.now().isAfter(otpData.expiryTime)) {
            log.warn("[OtpService] OTP expired for phone: {}", phone);
            otpStorage.remove(phone);
            return false;
        }
        
        // Verify OTP
        boolean isValid = otp.equals(otpData.otp);
        
        if (isValid) {
            log.info("[OtpService] ✓ OTP verified successfully for phone: {}", phone);
            // Remove OTP after successful verification
            otpStorage.remove(phone);
        } else {
            log.warn("[OtpService] ✗ Invalid OTP for phone: {}", phone);
        }
        
        return isValid;
    }
    
    /**
     * Clear OTP for a phone number
     */
    public void clearOtp(String phone) {
        otpStorage.remove(phone);
        log.info("[OtpService] Cleared OTP for phone: {}", phone);
    }
}
