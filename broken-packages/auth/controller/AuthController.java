package com.tiffin.api.auth.controller;

import com.tiffin.api.auth.dto.AuthResponse;
import com.tiffin.api.auth.dto.GoogleSignInRequest;
import com.tiffin.api.auth.dto.OtpRequest;
import com.tiffin.api.auth.dto.VerifyOtpRequest;
import com.tiffin.api.auth.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final AuthenticationService authService;

    @PostMapping("/send-otp")
    public ResponseEntity<AuthResponse> sendOtp(@RequestBody OtpRequest request) {
        log.info("� POST /auth/send-otp - Phone: {}", request.getPhone());
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

    @PostMapping("/verify-otp")
    public ResponseEntity<AuthResponse> verifyOtp(@RequestBody VerifyOtpRequest request) {
        log.info("� POST /auth/verify-otp - Phone: {}", request.getPhone());
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

    @PostMapping("/google-signin")
    public ResponseEntity<AuthResponse> googleSignIn(@RequestBody GoogleSignInRequest request) {
        log.info("🌐 POST /auth/google-signin");
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
}