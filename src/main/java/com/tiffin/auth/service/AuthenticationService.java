package com.tiffin.auth.service;

import com.tiffin.auth.dto.AuthResponse;
import com.tiffin.auth.dto.CompleteSignupRequest;
import com.tiffin.user.model.User;
import com.tiffin.user.model.Role;
import com.tiffin.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Authentication Service
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthenticationService {

    private final UserRepository userRepository;
    
    // In-memory OTP storage (in production, use Redis or database)
    private final Map<String, String> otpStore = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> otpExpiryStore = new ConcurrentHashMap<>();
    
    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 5;

    /**
     * Send OTP to phone number
     */
    public AuthResponse sendOtp(String phoneNumber) {
        try {
            log.info("Sending OTP to phone: {}", phoneNumber);
            
            // Generate 6-digit OTP
            String otp = generateOtp();
            
            // Store OTP with expiry time
            String otpKey = "otp:" + phoneNumber;
            otpStore.put(otpKey, otp);
            otpExpiryStore.put(otpKey, LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES));
            
            // In production, send actual SMS here
            log.info("Generated OTP for {}: {} (expires in {} minutes)", phoneNumber, otp, OTP_EXPIRY_MINUTES);
            
            // For development, return OTP in response (remove in production)
            return AuthResponse.builder()
                    .success(true)
                    .message("OTP sent successfully")
                    .otpId(otp) // Remove this in production
                    .build();
                    
        } catch (Exception e) {
            log.error("Error sending OTP to {}: {}", phoneNumber, e.getMessage(), e);
            return AuthResponse.error("Failed to send OTP");
        }
    }

    /**
     * Verify OTP and authenticate user
     */
    public AuthResponse verifyOtp(String phoneNumber, String otp) {
        try {
            log.info("Verifying OTP for phone: {}", phoneNumber);
            
            String otpKey = "otp:" + phoneNumber;
            String storedOtp = otpStore.get(otpKey);
            LocalDateTime expiry = otpExpiryStore.get(otpKey);
            
            // Check if OTP exists and is not expired
            if (storedOtp == null || expiry == null || LocalDateTime.now().isAfter(expiry)) {
                return AuthResponse.error("OTP expired or invalid");
            }
            
            // Verify OTP
            if (!storedOtp.equals(otp)) {
                return AuthResponse.error("Invalid OTP");
            }
            
            // Clear OTP from store
            otpStore.remove(otpKey);
            otpExpiryStore.remove(otpKey);
            
            // Find or create user
            User user = findOrCreateUser(phoneNumber);
            
            // Update last login time
            user.updateLastLoginTime();
            user.markPhoneAsVerified();
            userRepository.save(user);
            
            // Generate JWT token (simplified for now)
            String token = generateJwtToken(user);
            
            // Build user info
            AuthResponse.UserInfo userInfo = AuthResponse.UserInfo.builder()
                    .id(user.getId())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .email(user.getEmail())
                    .phone(user.getPhoneNumber())
                    .phoneVerified(user.isPhoneVerified())
                    .emailVerified(user.isEmailVerified())
                    .build();
            
            return AuthResponse.success("Login successful", token, userInfo);
            
        } catch (Exception e) {
            log.error("Error verifying OTP for {}: {}", phoneNumber, e.getMessage(), e);
            return AuthResponse.error("OTP verification failed");
        }
    }

    /**
     * Complete user signup with additional details
     */
    public AuthResponse completeSignup(CompleteSignupRequest request) {
        try {
            log.info("Completing signup for phone: {}", request.getPhone());
            
            // Find existing user or create new one
            User user = userRepository.findByPhoneNumber(request.getPhone())
                    .orElse(null);
            
            if (user == null) {
                // Create new user with all details
                user = User.builder()
                        .phoneNumber(request.getPhone())
                        .email(request.getEmail() != null ? request.getEmail() : request.getPhone() + "@tiffin.com")
                        .password(request.getPassword() != null ? request.getPassword() : "")
                        .firstName(extractFirstName(request.getName()))
                        .lastName(extractLastName(request.getName()))
                        .role(Role.USER)
                        .active(true)
                        .phoneVerified(true) // Assume phone is verified if we reach this point
                        .emailVerified(false)
                        .build();
                user = userRepository.save(user);
            } else {
                // Update existing user with additional details
                if (request.getEmail() != null && !request.getEmail().isEmpty()) {
                    user.setEmail(request.getEmail());
                }
                if (request.getName() != null && !request.getName().isEmpty()) {
                    user.setFirstName(extractFirstName(request.getName()));
                    user.setLastName(extractLastName(request.getName()));
                }
                if (request.getPassword() != null && !request.getPassword().isEmpty()) {
                    user.setPassword(request.getPassword());
                }
                user.markPhoneAsVerified();
                user = userRepository.save(user);
            }
            
            // Update last login time
            user.updateLastLoginTime();
            userRepository.save(user);
            
            // Generate JWT token
            String token = generateJwtToken(user);
            
            // Build user info
            AuthResponse.UserInfo userInfo = AuthResponse.UserInfo.builder()
                    .id(user.getId())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .email(user.getEmail())
                    .phone(user.getPhoneNumber())
                    .phoneVerified(user.isPhoneVerified())
                    .emailVerified(user.isEmailVerified())
                    .build();
            
            return AuthResponse.success("Registration successful", token, userInfo);
            
        } catch (Exception e) {
            log.error("Error completing signup for {}: {}", request.getPhone(), e.getMessage(), e);
            return AuthResponse.error("Registration failed");
        }
    }

    /**
     * Find existing user or create new one
     */
    private User findOrCreateUser(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber)
                .orElseGet(() -> createNewUser(phoneNumber));
    }

    /**
     * Create new user with phone number
     */
    private User createNewUser(String phoneNumber) {
        User user = User.builder()
                .phoneNumber(phoneNumber)
                .email(phoneNumber + "@tiffin.com") // Temporary email
                .password("") // No password for OTP login
                .firstName("User")
                .lastName("")
                .role(Role.USER)
                .active(true)
                .phoneVerified(false)
                .emailVerified(false)
                .build();
        
        return userRepository.save(user);
    }

    /**
     * Generate random OTP
     */
    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        StringBuilder otp = new StringBuilder();
        
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(random.nextInt(10));
        }
        
        return otp.toString();
    }

    /**
     * Generate JWT token (simplified implementation)
     */
    private String generateJwtToken(User user) {
        // In production, use proper JWT library like jjwt
        return "jwt_token_" + user.getId() + "_" + System.currentTimeMillis();
    }

    /**
     * Extract first name from full name
     */
    private String extractFirstName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "User";
        }
        String[] parts = fullName.trim().split("\\s+");
        return parts[0];
    }

    /**
     * Extract last name from full name
     */
    private String extractLastName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "";
        }
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length > 1) {
            StringBuilder lastName = new StringBuilder();
            for (int i = 1; i < parts.length; i++) {
                if (i > 1) lastName.append(" ");
                lastName.append(parts[i]);
            }
            return lastName.toString();
        }
        return "";
    }
}