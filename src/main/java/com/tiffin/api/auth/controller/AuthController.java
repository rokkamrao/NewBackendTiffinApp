package com.tiffin.api.auth.controller;

import com.tiffin.api.auth.dto.AuthRequest;
import com.tiffin.api.auth.dto.AuthResponse;
import com.tiffin.api.auth.dto.ForgotPasswordRequest;
import com.tiffin.api.auth.dto.ResetPasswordRequest;
import com.tiffin.api.auth.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody AuthRequest request) {
        log.info("📝 POST /auth/register - Phone: {}, Email: {}, Name: {}", 
                request.getPhone(), request.getEmail(), request.getName());
        try {
            AuthResponse response = authService.register(request);
            response.setSuccess(true);
            log.info("✓ Registration successful - Phone: {}", request.getPhone());
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException e) {
            log.error("✗ Registration failed - Phone: {}, Error: {}", request.getPhone(), e.getReason());
            throw e;
        } catch (Exception e) {
            log.error("✗ Registration failed - Phone: {}, Error: {}", request.getPhone(), e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        log.info("🔐 POST /auth/login - Phone: {}, Email: {}", request.getPhone(), request.getEmail());
        try {
            AuthResponse response = authService.authenticate(request);
            response.setSuccess(true);
            log.info("✓ Login successful - Phone: {}", request.getPhone());
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException e) {
            log.error("✗ Login failed - Phone: {}, Error: {}", request.getPhone(), e.getReason());
            throw e;
        } catch (Exception e) {
            log.error("✗ Login failed - Phone: {}, Error: {}", request.getPhone(), e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<AuthResponse> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        log.info("🔑 POST /auth/forgot-password - Phone: {}, Email: {}", request.getPhone(), request.getEmail());
        try {
            AuthResponse response = authService.sendPasswordResetOtp(request.getPhone(), request.getEmail());
            log.info("✓ Password reset OTP sent - Phone: {}, Email: {}", request.getPhone(), request.getEmail());
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException e) {
            log.error("✗ Forgot password failed - Phone: {}, Email: {}, Error: {}", 
                request.getPhone(), request.getEmail(), e.getReason());
            throw e;
        } catch (Exception e) {
            log.error("✗ Forgot password failed - Phone: {}, Email: {}, Error: {}", 
                request.getPhone(), request.getEmail(), e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<AuthResponse> resetPassword(@RequestBody ResetPasswordRequest request) {
        log.info("🔄 POST /auth/reset-password - Phone: {}, Email: {}", request.getPhone(), request.getEmail());
        try {
            AuthResponse response = authService.resetPassword(
                request.getPhone(),
                request.getEmail(),
                request.getOtp(), 
                request.getNewPassword()
            );
            log.info("✓ Password reset successful - Phone: {}, Email: {}", request.getPhone(), request.getEmail());
            return ResponseEntity.ok(response);
        } catch (ResponseStatusException e) {
            log.error("✗ Password reset failed - Phone: {}, Email: {}, Error: {}", 
                request.getPhone(), request.getEmail(), e.getReason());
            throw e;
        } catch (Exception e) {
            log.error("✗ Password reset failed - Phone: {}, Email: {}, Error: {}", 
                request.getPhone(), request.getEmail(), e.getMessage(), e);
            throw e;
        }
    }
}