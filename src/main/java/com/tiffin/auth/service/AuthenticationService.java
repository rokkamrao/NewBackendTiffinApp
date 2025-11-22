package com.tiffin.auth.service;

import com.tiffin.auth.dto.AuthResponse;
import com.tiffin.auth.dto.CompleteSignupRequest;
import com.tiffin.auth.dto.SignInRequest;
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
            log.info("🔐 Verifying OTP for phone: {} with OTP: {}", phoneNumber, otp);
            
            String otpKey = "otp:" + phoneNumber;
            String storedOtp = otpStore.get(otpKey);
            LocalDateTime expiry = otpExpiryStore.get(otpKey);
            
            // Development mode: Accept "123456" as universal OTP for testing
            boolean isDevelopmentOtp = "123456".equals(otp);
            
            log.info("🔍 OTP Check - Phone: {}, Received: {}, Stored: {}, IsDev: {}", phoneNumber, otp, storedOtp, isDevelopmentOtp);
            
            if (isDevelopmentOtp) {
                log.info("🚀 DEVELOPMENT OTP BYPASS: 123456 used - allowing authentication");
            } else {
                // Check if OTP exists and is not expired
                if (storedOtp == null || expiry == null || LocalDateTime.now().isAfter(expiry)) {
                    log.warn("❌ OTP expired or invalid - Phone: {}, StoredOtp: {}, Expiry: {}", phoneNumber, storedOtp, expiry);
                    return AuthResponse.error("OTP expired or invalid");
                }
                
                // Verify OTP
                if (!storedOtp.equals(otp)) {
                    log.warn("❌ OTP mismatch - Phone: {}, Expected: {}, Received: {}", phoneNumber, storedOtp, otp);
                    return AuthResponse.error("Invalid OTP");
                }
                
                log.info("✅ OTP verified successfully - Phone: {}", phoneNumber);
            }
            
            // Clear OTP from store (unless it's development OTP)
            if (!isDevelopmentOtp) {
                otpStore.remove(otpKey);
                otpExpiryStore.remove(otpKey);
            }
            
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
                    .role(user.getRole().toString())
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
                User newUser = User.builder()
                        .phoneNumber(request.getPhone())
                        .email(request.getEmail() != null ? request.getEmail() : request.getPhone() + "@tiffin.com")
                        .password(request.getPassword() != null ? request.getPassword() : "")
                        .firstName(request.getFirstName() != null ? request.getFirstName() : "User")
                        .lastName(request.getLastName() != null ? request.getLastName() : "")
                        .role(Role.USER)
                        .active(true)
                        .phoneVerified(true) // Assume phone is verified if we reach this point
                        .emailVerified(false)
                        .build();
                @SuppressWarnings("null")
                User savedUser = userRepository.save(newUser);
                user = savedUser;
            } else {
                // Update existing user with additional details
                if (request.getEmail() != null && !request.getEmail().isEmpty()) {
                    user.setEmail(request.getEmail());
                }
                if (request.getFirstName() != null && !request.getFirstName().isEmpty()) {
                    user.setFirstName(request.getFirstName());
                }
                if (request.getLastName() != null && !request.getLastName().isEmpty()) {
                    user.setLastName(request.getLastName());
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
                    .role(user.getRole().toString())
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
        
        @SuppressWarnings("null")
        User savedUser = userRepository.save(user);
        return savedUser;
    }

    /**
     * Login user with phone number or email and password
     */
    public AuthResponse login(SignInRequest request) {
        try {
            log.info("Login attempt for: {}", request.getPhone());
            
            // Find user by phone number or email
            User user = userRepository.findByPhoneNumber(request.getPhone())
                    .or(() -> userRepository.findByEmail(request.getPhone()))
                    .orElse(null);
            
            if (user == null) {
                return AuthResponse.error("User not found");
            }
            
            // For now, we'll do simple password comparison
            // In production, use BCrypt or similar
            if (!request.getPassword().equals(user.getPassword())) {
                return AuthResponse.error("Invalid credentials");
            }
            
            if (!user.isPhoneVerified()) {
                return AuthResponse.error("Phone number not verified");
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
                    .role(user.getRole().toString())
                    .build();
            
            return AuthResponse.success("Login successful", token, userInfo);
            
        } catch (Exception e) {
            log.error("Error during login for {}: {}", request.getPhone(), e.getMessage(), e);
            return AuthResponse.error("Login failed");
        }
    }

    /**
     * Generate OTP (fixed for development, random for production)
     */
    /**
     * Validate session token and return user data
     */
    public AuthResponse validateSession(String token) {
        try {
            log.info("🔍 Validating session token");
            
            // In a real JWT implementation, you would:
            // 1. Parse and validate the JWT token
            // 2. Check token expiration
            // 3. Verify signature
            // 4. Extract user ID from token claims
            
            // Validate token format
            if (token == null || token.trim().isEmpty()) {
                return AuthResponse.error("Invalid token");
            }
            
            // Extract user ID from simple token format (jwt_token_{userId}_{timestamp})
            // Note: This is a simplified implementation. In production, use proper JWT libraries
            if (!token.startsWith("jwt_token_")) {
                return AuthResponse.error("Invalid token format");
            }
            
            try {
                String[] parts = token.split("_");
                if (parts.length < 3) {
                    return AuthResponse.error("Invalid token format");
                }
                
                Long userId = Long.parseLong(parts[2]);
                long timestamp = Long.parseLong(parts[3]);
                
                // Check token age (24 hours expiry)
                long currentTime = System.currentTimeMillis();
                long tokenAge = currentTime - timestamp;
                long maxAge = 24 * 60 * 60 * 1000; // 24 hours in milliseconds
                
                if (tokenAge > maxAge) {
                    return AuthResponse.error("Token expired");
                }
                
                // Find user by ID
                User user = userRepository.findById(userId).orElse(null);
                if (user == null || !user.isActive()) {
                    return AuthResponse.error("User not found or inactive");
                }
                
                log.info("✅ Token validation successful for user: {}", userId);
                
                // Build user info response
                AuthResponse.UserInfo userInfo = AuthResponse.UserInfo.builder()
                        .id(user.getId())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .email(user.getEmail())
                        .phone(user.getPhoneNumber())
                        .phoneVerified(user.isPhoneVerified())
                        .emailVerified(user.isEmailVerified())
                        .role(user.getRole().toString())
                        .build();
                
                return AuthResponse.success("Token valid", token, userInfo);
                
            } catch (NumberFormatException e) {
                return AuthResponse.error("Invalid token format");
            }
            
        } catch (Exception e) {
            log.error("❌ Token validation failed: {}", e.getMessage(), e);
            return AuthResponse.error("Token validation failed");
        }
    }
    
    /**
     * Logout user and invalidate token
     */
    public void logout(String token) {
        try {
            log.info("🚪 Logging out user with token");
            
            // In a real implementation:
            // 1. Add token to blacklist/revocation list
            // 2. Clear any server-side session data
            // 3. Optionally notify other services
            
            // For now, just log the logout
            log.info("✅ User logged out successfully");
            
        } catch (Exception e) {
            log.error("❌ Logout failed: {}", e.getMessage(), e);
        }
    }

    private String generateOtp() {
        // For development: use fixed OTP 123456
        // For production: use random OTP
        String environment = System.getProperty("spring.profiles.active", "development");
        
        if ("production".equals(environment)) {
            SecureRandom random = new SecureRandom();
            StringBuilder otp = new StringBuilder();
            
            for (int i = 0; i < OTP_LENGTH; i++) {
                otp.append(random.nextInt(10));
            }
            
            return otp.toString();
        } else {
            // Development mode: always return 123456
            return "123456";
        }
    }

    /**
     * Generate JWT token (simplified implementation)
     */
    private String generateJwtToken(User user) {
        // In production, use proper JWT library like jjwt
        return "jwt_token_" + user.getId() + "_" + System.currentTimeMillis();
    }

}