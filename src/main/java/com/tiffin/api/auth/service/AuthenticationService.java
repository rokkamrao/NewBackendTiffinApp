package com.tiffin.api.auth.service;

import com.tiffin.api.auth.dto.AuthRequest;
import com.tiffin.api.auth.dto.AuthResponse;
import com.tiffin.api.auth.security.JwtTokenProvider;
import com.tiffin.api.user.model.User;
import com.tiffin.api.user.model.Address;
import com.tiffin.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;

    public AuthResponse register(AuthRequest request) {
        log.info("[AuthService] register() - Phone: {}, Email: {}, Name: {}", request.getPhone(), request.getEmail(), request.getName());
        
        // Validate required fields
        if (request.getPhone() == null || request.getPhone().isBlank()) {
            log.error("[AuthService] register() - Phone is missing");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Phone number is required");
        }
        
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            log.error("[AuthService] register() - Password is missing");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required");
        }
        
        if (request.getName() == null || request.getName().isBlank()) {
            log.error("[AuthService] register() - Name is missing");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name is required");
        }
        
        // Check if phone already exists
        if (userRepository.existsByPhone(request.getPhone())) {
            log.warn("[AuthService] register() - Phone already exists: {}", request.getPhone());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This phone number is already registered. Please login instead.");
        }
        
        // Check if email already exists (if provided)
        if (request.getEmail() != null && !request.getEmail().isBlank() && 
            userRepository.existsByEmail(request.getEmail())) {
            log.warn("[AuthService] register() - Email already exists: {}", request.getEmail());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This email is already registered. Please login instead.");
        }
        
        try {
            // Create user with all signup data
            User user = User.builder()
                    .phone(request.getPhone())
                    .email(request.getEmail())
                    .name(request.getName())
                    .password(passwordEncoder.encode(request.getPassword()))
                    .referralCode(request.getReferral())
                    .emailNotificationsEnabled(request.getNewsletter() != null ? request.getNewsletter() : false)
                    .build();
            
            // Add dietary preferences if provided
            if (request.getDietary() != null && !request.getDietary().isEmpty()) {
                user.setDietaryPreferences(new HashSet<>(request.getDietary()));
                log.info("[AuthService] register() - Dietary preferences added: {}", request.getDietary());
            }
            
            // Add allergies if provided
            if (request.getAllergies() != null && !request.getAllergies().isEmpty()) {
                user.setAllergies(new HashSet<>(request.getAllergies()));
                log.info("[AuthService] register() - Allergies added: {}", request.getAllergies());
            }

            // Save user first to get the ID
            user = userRepository.save(user);
            log.info("[AuthService] register() - User saved to database: {}", user.getPhone());
            
            // Add address if provided
            if (request.getAddress() != null && 
                request.getAddress().getPincode() != null && 
                !request.getAddress().getPincode().isBlank()) {
                
                Address address = Address.builder()
                        .user(user)
                        .pincode(request.getAddress().getPincode())
                        .city(request.getAddress().getCity())
                        .addressLine1(request.getAddress().getArea()) // Using area as addressLine1
                        .state("Telangana") // Default state
                        .latitude(17.385044) // Default Hyderabad coordinates
                        .longitude(78.486671)
                        .isDefault(true)
                        .type("Home")
                        .build();
                
                Set<Address> addresses = new HashSet<>();
                addresses.add(address);
                user.setAddresses(addresses);
                
                user = userRepository.save(user);
                log.info("[AuthService] register() - Address added: {}, {}, {}", 
                    request.getAddress().getArea(), 
                    request.getAddress().getCity(), 
                    request.getAddress().getPincode());
            }
            
            String token = jwtTokenProvider.generateToken(user);
            log.info("[AuthService] register() - JWT token generated for: {}", user.getPhone());
            
            return buildAuthResponse(user, token);
        } catch (Exception e) {
            log.error("[AuthService] register() - Error saving user: {}", request.getPhone(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Registration failed. Please try again later.");
        }
    }

    public AuthResponse authenticate(AuthRequest request) {
        log.info("[AuthService] authenticate() - Phone: {}, Email: {}", request.getPhone(), request.getEmail());
        
        // Validate input
        if ((request.getPhone() == null || request.getPhone().isBlank()) && 
            (request.getEmail() == null || request.getEmail().isBlank())) {
            log.error("[AuthService] authenticate() - Both phone and email are missing");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Phone or email is required");
        }
        
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            log.error("[AuthService] authenticate() - Password is missing");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required");
        }
        
        // Determine identifier (phone or email)
        String identifier = request.getPhone() != null && !request.getPhone().isBlank() 
                ? request.getPhone() 
                : request.getEmail();
        
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(identifier, request.getPassword())
            );
            log.info("[AuthService] authenticate() - Authentication successful: {}", identifier);
        } catch (org.springframework.security.core.AuthenticationException e) {
            log.error("[AuthService] authenticate() - Authentication failed for {}: {}", identifier, e.getMessage());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials. Please check your phone/email and password.");
        } catch (Exception e) {
            log.error("[AuthService] authenticate() - Unexpected error during authentication: {}", identifier, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Authentication service error. Please try again later.");
        }

        // Find user by phone or email
        User user;
        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            user = userRepository.findByPhone(request.getPhone())
                    .orElseThrow(() -> {
                        log.error("[AuthService] authenticate() - User not found by phone: {}", request.getPhone());
                        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
                    });
        } else {
            user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> {
                        log.error("[AuthService] authenticate() - User not found by email: {}", request.getEmail());
                        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
                    });
        }
                
        String token = jwtTokenProvider.generateToken(user);
        log.info("[AuthService] authenticate() - JWT token generated for: {}", user.getPhone());
        
        return buildAuthResponse(user, token);
    }
    
    public AuthResponse sendPasswordResetOtp(String phone, String email) {
        log.info("[AuthService] sendPasswordResetOtp() - Phone: {}, Email: {}", phone, email);
        
        // Validate that at least one identifier is provided
        if ((phone == null || phone.isBlank()) && (email == null || email.isBlank())) {
            log.error("[AuthService] sendPasswordResetOtp() - Both phone and email are missing");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Phone number or email is required");
        }
        
        // Determine identifier and validate user exists
        String identifier;
        
        if (phone != null && !phone.isBlank()) {
            identifier = phone;
            userRepository.findByPhone(phone)
                    .orElseThrow(() -> {
                        log.error("[AuthService] sendPasswordResetOtp() - User not found by phone: {}", phone);
                        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Phone number not registered");
                    });
        } else {
            identifier = email;
            userRepository.findByEmail(email)
                    .orElseThrow(() -> {
                        log.error("[AuthService] sendPasswordResetOtp() - User not found by email: {}", email);
                        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Email not registered");
                    });
        }
        
        // Generate and send OTP
        otpService.generateOtp(identifier);
        log.info("[AuthService] sendPasswordResetOtp() - OTP sent to: {}", identifier);
        
        return AuthResponse.builder()
                .success(true)
                .message("OTP sent successfully to " + identifier)
                .build();
    }
    
    public AuthResponse resetPassword(String phone, String email, String otp, String newPassword) {
        log.info("[AuthService] resetPassword() - Phone: {}, Email: {}", phone, email);
        
        // Validate that at least one identifier is provided
        if ((phone == null || phone.isBlank()) && (email == null || email.isBlank())) {
            log.error("[AuthService] resetPassword() - Both phone and email are missing");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Phone number or email is required");
        }
        
        if (otp == null || otp.isBlank()) {
            log.error("[AuthService] resetPassword() - OTP is missing");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP is required");
        }
        
        if (newPassword == null || newPassword.isBlank()) {
            log.error("[AuthService] resetPassword() - Password is missing");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New password is required");
        }
        
        if (newPassword.length() < 8) {
            log.error("[AuthService] resetPassword() - Password too short");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at least 8 characters");
        }
        
        // Determine identifier
        String identifier = (phone != null && !phone.isBlank()) ? phone : email;
        
        // Verify OTP
        boolean isOtpValid = otpService.verifyOtp(identifier, otp);
        if (!isOtpValid) {
            log.error("[AuthService] resetPassword() - Invalid OTP for: {}", identifier);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired OTP");
        }
        
        // Find user
        User user;
        if (phone != null && !phone.isBlank()) {
            user = userRepository.findByPhone(phone)
                    .orElseThrow(() -> {
                        log.error("[AuthService] resetPassword() - User not found by phone: {}", phone);
                        return new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
                    });
        } else {
            user = userRepository.findByEmail(email)
                    .orElseThrow(() -> {
                        log.error("[AuthService] resetPassword() - User not found by email: {}", email);
                        return new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
                    });
        }
        
        // Update password with BCrypt encoding
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);
        User savedUser = userRepository.save(user);
        
        log.info("[AuthService] resetPassword() - Password updated successfully for: {} (ID: {})", 
            identifier, savedUser.getId());
        log.debug("[AuthService] resetPassword() - New password hash starts with: {}", 
            encodedPassword.substring(0, Math.min(20, encodedPassword.length())));
        
        return AuthResponse.builder()
                .success(true)
                .message("Password reset successfully")
                .build();
    }
    
    private AuthResponse buildAuthResponse(User user, String token) {
        return AuthResponse.builder()
                .success(true)
                .token(token)
                .phone(user.getPhone())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .dietary(user.getDietaryPreferences() != null ? 
                    user.getDietaryPreferences().stream().toList() : null)
                .allergies(user.getAllergies() != null ? 
                    user.getAllergies().stream().toList() : null)
                .referralCode(user.getReferralCode())
                .emailNotifications(user.isEmailNotificationsEnabled())
                .build();
    }
}