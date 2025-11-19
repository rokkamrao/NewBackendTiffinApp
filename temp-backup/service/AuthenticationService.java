package com.tiffin.auth.service;

import com.tiffin.auth.dto.AuthResponse;
import com.tiffin.user.model.User;
import com.tiffin.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.time.LocalDateTime;

/**
 * Authentication Service
 * 
 * Handles OTP generation, verification, and Google Sign-In
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {
    
    private final UserRepository userRepository;
    
    // In-memory OTP storage (for development - replace with Redis in production)
    private final ConcurrentHashMap<String, OtpData> otpStorage = new ConcurrentHashMap<>();
    
    // Demo OTP for testing purposes
    private static final String DEMO_OTP = "123456";
    
    /**
     * Send OTP to phone number
     * 
     * @param phone The phone number
     * @return AuthResponse with OTP details
     */
    public AuthResponse sendOtp(String phone) {
        log.info("📱 Sending OTP to phone: {}", phone);
        
        try {
            // For demo purposes, use a fixed OTP
            String otp = DEMO_OTP;
            String otpId = "otp_" + System.currentTimeMillis();
            
            // Store OTP with expiration (30 minutes for demo)
            OtpData otpData = new OtpData(otp, phone, LocalDateTime.now().plusMinutes(30));
            otpStorage.put(otpId, otpData);
            
            // Log the demo OTP for easy testing
            log.info("🔐 Generated DEMO OTP for {}: {} (OTP ID: {})", phone, otp, otpId);
            
            // TODO: In production, integrate with SMS service (Twilio, AWS SNS, etc.)
            // For demo, we use a fixed OTP that users can easily enter
            
            return AuthResponse.successWithOtp("OTP sent successfully. Use demo OTP: " + DEMO_OTP, otpId);
            
        } catch (Exception e) {
            log.error("❌ Failed to send OTP to {}: {}", phone, e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to send OTP");
        }
    }
    
    /**
     * Verify OTP and authenticate user
     * 
     * @param phone The phone number
     * @param otp The OTP to verify
     * @return AuthResponse with user details and token
     */
    @SuppressWarnings("null")
    public AuthResponse verifyOtp(String phone, String otp) {
        log.info("🔐 Verifying OTP for phone: {} with OTP: {}", phone, otp);
        
        try {
            // Find matching OTP
            Optional<String> otpId = otpStorage.entrySet().stream()
                    .filter(entry -> {
                        OtpData data = entry.getValue();
                        boolean phoneMatch = data.phone.equals(phone);
                        boolean otpMatch = data.otp.equals(otp);
                        boolean notExpired = data.expiresAt.isAfter(LocalDateTime.now());
                        
                        log.info("🔍 OTP verification: phone match={}, otp match={}, not expired={}", 
                            phoneMatch, otpMatch, notExpired);
                        
                        return phoneMatch && otpMatch && notExpired;
                    })
                    .map(entry -> entry.getKey())
                    .findFirst();
            
            if (otpId.isEmpty()) {
                log.warn("❌ Invalid or expired OTP for phone: {}", phone);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired OTP. Please use demo OTP: " + DEMO_OTP);
            }
            
            // Remove used OTP
            otpStorage.remove(otpId.get());
            
            // Find or create user
            Optional<User> existingUser = userRepository.findByPhoneNumber(phone);
            User user;
            
            if (existingUser.isPresent()) {
                user = existingUser.get();
                user.markPhoneAsVerified();
                log.info("✓ Existing user found: {}", user.getId());
            } else {
                // Create new user
                user = User.builder()
                        .phoneNumber(phone)
                        .phoneVerified(true)
                        .email("user" + phone + "@tiffin.com") // Temporary email
                        .password("temp_password") // Temporary password
                        .build();
                log.info("➕ Creating new user for phone: {}", phone);
            }
            
            user = userRepository.save(user);
            
            // Create user info for response
            AuthResponse.UserInfo userInfo = AuthResponse.UserInfo.builder()
                    .id(user.getId())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .email(user.getEmail())
                    .phone(user.getPhoneNumber())
                    .profilePictureUrl(user.getProfileImageUrl())
                    .phoneVerified(user.isPhoneVerified())
                    .emailVerified(user.isEmailVerified())
                    .build();
            
            // TODO: Generate JWT token
            String token = "temp_token_" + user.getId() + "_" + System.currentTimeMillis();
            
            log.info("✓ OTP verification successful for phone: {}, user ID: {}", phone, user.getId());
            return AuthResponse.success("Login successful", token, userInfo);
            
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("❌ OTP verification failed for phone {}: {}", phone, e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "OTP verification failed");
        }
    }
    
    /**
     * Google Sign-In authentication
     * 
     * @param token Google OAuth token
     * @return AuthResponse with user details
     */
    public AuthResponse googleSignIn(String token) {
        log.info("🌐 Processing Google Sign-In");
        
        try {
            // TODO: Verify Google token with Google APIs
            // For now, create a mock response
            
            log.warn("⚠️ Google Sign-In not fully implemented yet");
            throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Google Sign-In coming soon");
            
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.error("❌ Google Sign-In failed: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Google Sign-In failed");
        }
    }
    
    /**
     * OTP data storage class
     */
    private static class OtpData {
        final String otp;
        final String phone;
        final LocalDateTime expiresAt;
        
        OtpData(String otp, String phone, LocalDateTime expiresAt) {
            this.otp = otp;
            this.phone = phone;
            this.expiresAt = expiresAt;
        }
    }
}