package com.tiffin.api.auth.service;

import com.tiffin.api.auth.dto.AuthResponse;
import com.tiffin.api.auth.security.JwtTokenProvider;
import com.tiffin.api.user.model.User;
import com.tiffin.api.user.model.Role;
import com.tiffin.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private static final Logger log = LoggerFactory.getLogger(AuthenticationService.class);
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;
    private final GoogleSignInService googleSignInService;

    public AuthResponse sendOtp(String phone) {
        log.info("[AuthService] sendOtp() - Phone: {}", phone);
        if (phone == null || phone.isBlank()) {
            log.error("[AuthService] sendOtp() - Phone is missing");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Phone number is required");
        }
        
        otpService.generateOtp(phone);
        log.info("[AuthService] sendOtp() - OTP sent to: {}", phone);
        return AuthResponse.builder()
                .success(true)
                .message("OTP sent successfully to " + phone)
                .build();
    }

    public AuthResponse verifyOtp(String phone, String otp) {
        log.info("[AuthService] verifyOtp() - Phone: {}", phone);
        if (phone == null || phone.isBlank() || otp == null || otp.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Phone and OTP are required");
        }

        boolean isOtpValid = otpService.verifyOtp(phone, otp);
        if (!isOtpValid) {
            log.error("[AuthService] verifyOtp() - Invalid OTP for: {}", phone);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired OTP");
        }

        User user = userRepository.findByPhone(phone).orElse(null);
        boolean isNewUser = user == null;

        if (isNewUser) {
            log.info("[AuthService] verifyOtp() - New user, creating account for phone: {}", phone);
            user = User.builder()
                    .phone(phone)
                    .role(Role.CUSTOMER)
                    .isActive(true)
                    .build();
            user = userRepository.save(user);
            log.info("[AuthService] verifyOtp() - New user created with ID: {}", user.getId());
        }

        String token = jwtTokenProvider.generateToken(user);
        log.info("[AuthService] verifyOtp() - JWT token generated for: {}", user.getPhone());
        return buildAuthResponse(user, token, isNewUser);
    }

    public AuthResponse googleSignIn(String idTokenString) {
        log.info("[AuthService] googleSignIn() - Verifying Google ID token");
        Map<String, Object> payload = googleSignInService.verifyToken(idTokenString);
        if (payload == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Google ID token");
        }

        String email = (String) payload.get("email");
        String name = (String) payload.get("name");
        
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email not provided by Google");
        }

        User user = userRepository.findByEmail(email).orElse(null);
        boolean isNewUser = user == null;

        if (isNewUser) {
            log.info("[AuthService] googleSignIn() - New user, creating account for email: {}", email);
            user = User.builder()
                    .email(email)
                    .name(name)
                    .role(Role.CUSTOMER)
                    .isActive(true)
                    .build();
            user = userRepository.save(user);
            log.info("[AuthService] googleSignIn() - New user created with ID: {}", user.getId());
        }

        String token = jwtTokenProvider.generateToken(user);
        log.info("[AuthService] googleSignIn() - JWT token generated for: {}", user.getEmail());
        return buildAuthResponse(user, token, isNewUser);
    }

    private AuthResponse buildAuthResponse(User user, String token, boolean isNewUser) {
        return AuthResponse.builder()
                .success(true)
                .token(token)
                .phone(user.getPhone())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .isNewUser(isNewUser)
                .build();
    }
}