package com.tiffin.auth.controller;

import com.tiffin.auth.dto.AuthResponse;
import com.tiffin.auth.dto.CompleteSignupRequest;
import com.tiffin.auth.dto.OtpRequest;
import com.tiffin.auth.dto.SignInRequest;
import com.tiffin.auth.dto.VerifyOtpRequest;
import com.tiffin.auth.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * Authentication Controller
 * 
 * Handles authentication operations:
 * - OTP sending and verification
 * - User registration and login
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = {"https://tiffin-self.vercel.app", "http://localhost:4200"})
@Slf4j
@Tag(name = "Authentication", description = "User authentication and registration endpoints")
public class AuthController {
    
    private final AuthenticationService authService;

    /**
     * Send OTP to user's phone number
     * 
     * @param request OTP request containing phone number
     * @return Response with OTP status
     */
    @Operation(
        summary = "Send OTP to phone number",
        description = """
            Sends a One-Time Password (OTP) to the provided phone number for verification.
            
            **Process:**
            1. Validates the phone number format
            2. Generates a 6-digit OTP code
            3. Sends the OTP via SMS (simulated in development)
            4. Returns the OTP ID for verification
            
            **Phone Number Format:** Should include country code (e.g., +1234567890)
            """,
        tags = {"Authentication"}
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200", 
            description = "OTP sent successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthResponse.class),
                examples = @ExampleObject(
                    name = "Success Response",
                    value = """
                        {
                            "success": true,
                            "message": "OTP sent successfully",
                            "otpId": "123456",
                            "user": null,
                            "token": null
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid phone number format",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Error Response",
                    value = """
                        {
                            "success": false,
                            "message": "Invalid phone number format",
                            "otpId": null,
                            "user": null,
                            "token": null
                        }
                        """
                )
            )
        )
    })
    @PostMapping("/send-otp")
    public ResponseEntity<AuthResponse> sendOtp(
            @Parameter(description = "OTP request containing phone number", required = true)
            @Valid @RequestBody OtpRequest request) {
        log.info("📱 POST /api/auth/send-otp - Phone: {}", request.getPhone());
        try {
            AuthResponse response = authService.sendOtp(request.getPhone());
            
            if (response.isSuccess()) {
                log.info("✓ OTP sent successfully - Phone: {}", request.getPhone());
            } else {
                log.warn("⚠ OTP sending failed - Phone: {}, Message: {}", request.getPhone(), response.getMessage());
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("✗ Send OTP failed - Phone: {}, Error: {}", request.getPhone(), e.getMessage(), e);
            return ResponseEntity.ok(AuthResponse.error("Failed to send OTP: " + e.getMessage()));
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
            
            if (response.isSuccess()) {
                log.info("✓ OTP verification successful - Phone: {}", request.getPhone());
            } else {
                log.warn("⚠ OTP verification failed - Phone: {}, Message: {}", request.getPhone(), response.getMessage());
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("✗ OTP verification failed - Phone: {}, Error: {}", request.getPhone(), e.getMessage(), e);
            return ResponseEntity.ok(AuthResponse.error("OTP verification failed: " + e.getMessage()));
        }
    }

    /**
     * User sign-in with phone/email and password
     * 
     * @param request Sign-in request with credentials
     * @return Response with user data and JWT token
     */
    @Operation(
        summary = "Sign in with credentials",
        description = "Authenticate user with phone number/email and password",
        tags = {"Authentication"}
    )
    @PostMapping("/sign-in")
    public ResponseEntity<AuthResponse> signIn(@Valid @RequestBody SignInRequest request) {
        log.info("🔑 POST /api/auth/sign-in - Phone/Email: {}", request.getPhone());
        try {
            AuthResponse response = authService.login(request);
            
            if (response.isSuccess()) {
                log.info("✓ Sign-in successful - Phone/Email: {}", request.getPhone());
            } else {
                log.warn("⚠ Sign-in failed - Phone/Email: {}, Message: {}", request.getPhone(), response.getMessage());
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("✗ Sign-in failed - Phone/Email: {}, Error: {}", request.getPhone(), e.getMessage(), e);
            return ResponseEntity.ok(AuthResponse.error("Sign-in failed: " + e.getMessage()));
        }
    }

    /**
     * Complete user signup with additional details
     * 
     * @param request Signup completion request with user details
     * @return Response with user data and JWT token
     */
    @PostMapping("/complete-signup")
    public ResponseEntity<AuthResponse> completeSignup(@Valid @RequestBody CompleteSignupRequest request) {
        log.info("📝 POST /api/auth/complete-signup - Phone: {}, Email: {}", request.getPhone(), request.getEmail());
        try {
            AuthResponse response = authService.completeSignup(request);
            
            if (response.isSuccess()) {
                log.info("✓ User signup completed - Phone: {}", request.getPhone());
            } else {
                log.warn("⚠ User signup failed - Phone: {}, Message: {}", request.getPhone(), response.getMessage());
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("✗ User signup failed - Phone: {}, Error: {}", request.getPhone(), e.getMessage(), e);
            return ResponseEntity.ok(AuthResponse.error("Registration failed: " + e.getMessage()));
        }
    }

    /**
     * Health check endpoint for auth service
     * 
     * @return Simple health status
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        log.info("💚 GET /api/auth/health - Health check requested");
        return ResponseEntity.ok("Auth service is running");
    }
}