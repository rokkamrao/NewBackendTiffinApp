package com.tiffin.auth.controller;

import com.tiffin.auth.dto.AuthResponse;
import com.tiffin.auth.dto.OtpRequest;
import com.tiffin.auth.dto.VerifyOtpRequest;
import com.tiffin.auth.dto.GoogleSignInRequest;
import com.tiffin.auth.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;

/**
 * Authentication Controller
 * 
 * Handles basic authentication operations:
 * - OTP sending and verification
 * - Google Sign-In
 * - User registration and login
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class AuthController {
    
    private final AuthenticationService authService;

    /**
     * Send OTP to user's phone number
     * 
     * @param request OTP request containing phone number
     * @return Response with OTP status
     */
    @PostMapping("/send-otp")
    public ResponseEntity<AuthResponse> sendOtp(@Valid @RequestBody OtpRequest request) {
        log.info("📱 POST /api/auth/send-otp - Phone: {}", request.getPhone());
        try {
            AuthResponse response = authService.sendOtp(request.getPhone());
            log.info("✓ OTP sent successfully - Phone: {}", request.getPhone());
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException e) {
            log.error("✗ Send OTP failed - Phone: {}, Error: {}", request.getPhone(), e.getReason());
            throw e;
        } catch (Exception e) {
            log.error("✗ Send OTP failed - Phone: {}, Error: {}", request.getPhone(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Verify OTP sent to user's phone
     * 
     * @param request Verification request with phone and OTP
     * @return Response with JWT token if successful
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<AuthResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        log.info("🔐 POST /api/auth/verify-otp - Phone: {}", request.getPhone());
        try {
            AuthResponse response = authService.verifyOtp(request.getPhone(), request.getOtp());
            log.info("✓ OTP verification successful - Phone: {}", request.getPhone());
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException e) {
            log.error("✗ OTP verification failed - Phone: {}, Error: {}", request.getPhone(), e.getReason());
            throw e;
        } catch (Exception e) {
            log.error("✗ OTP verification failed - Phone: {}, Error: {}", request.getPhone(), e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Google Sign-In authentication
     * 
     * @param request Google sign-in token
     * @return Response with user details and JWT token
     */
    @PostMapping("/google-signin")
    public ResponseEntity<AuthResponse> googleSignIn(@Valid @RequestBody GoogleSignInRequest request) {
        log.info("🌐 POST /api/auth/google-signin");
        try {
            AuthResponse response = authService.googleSignIn(request.getToken());
            log.info("✓ Google Sign-In successful");
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException e) {
            log.error("✗ Google Sign-In failed - Error: {}", e.getReason());
            throw e;
        } catch (Exception e) {
            log.error("✗ Google Sign-In failed - Error: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Health check endpoint for auth service
     * 
     * @return Simple health status
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Auth service is running");
    }
}