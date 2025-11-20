package com.tiffin.auth.service;

import com.tiffin.auth.dto.CompleteSignupRequest;
import com.tiffin.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for AuthenticationService
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthenticationServiceTest {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void contextLoads() {
        assertThat(authenticationService).isNotNull();
        assertThat(userRepository).isNotNull();
    }

    @Test
    void sendOtp_NewPhoneNumber_Success() {
        String phoneNumber = "+919876543210";
        
        var response = authenticationService.sendOtp(phoneNumber);
        
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("OTP sent successfully");
    }

    @Test
    void completeSignup_ValidDetails_Success() {
        CompleteSignupRequest request = new CompleteSignupRequest();
        request.setPhone("+919876543210");
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john.doe@example.com");
        request.setPassword("SecurePass123!");
        request.setConfirmPassword("SecurePass123!");
        request.setPreferredLanguage("en");
        
        var response = authenticationService.completeSignup(request);
        
        assertThat(response).isNotNull();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Registration successful");
        assertThat(response.getUser()).isNotNull();
        assertThat(response.getUser().getFirstName()).isEqualTo("John");
        assertThat(response.getUser().getLastName()).isEqualTo("Doe");
    }
}