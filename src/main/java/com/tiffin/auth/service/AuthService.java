package com.tiffin.auth.service;

import com.tiffin.auth.dto.AuthDtos;
import com.tiffin.security.JwtTokenProvider;
import com.tiffin.user.model.User;
import com.tiffin.user.model.Role;
import com.tiffin.user.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    // OTP storage with expiry
    private final Map<String, String> otpStorage = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> otpExpiry = new ConcurrentHashMap<>();
    private static final int OTP_VALID_MINUTES = 5;

    public AuthService(AuthenticationManager authenticationManager, UserRepository userRepository,
            PasswordEncoder passwordEncoder, JwtTokenProvider tokenProvider) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
    }

    public AuthDtos.JwtResponse login(AuthDtos.LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        User user = userRepository.findByEmail(loginRequest.getEmail()).orElseThrow();

        // Update last login
        user.updateLastLoginTime();
        userRepository.save(user);

        return new AuthDtos.JwtResponse(jwt, user.getId(), user.getFullName(), user.getEmail(), user.getPhoneNumber(),
                user.getRole().name(), false);
    }

    public AuthDtos.JwtResponse signup(AuthDtos.SignupRequest signupRequest) {
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new RuntimeException("Email is already taken!");
        }
        if (userRepository.existsByPhoneNumber(signupRequest.getPhone())) {
            throw new RuntimeException("Phone is already taken!");
        }

        User user = User.builder()
                .firstName(signupRequest.getName().split(" ")[0])
                .lastName(signupRequest.getName().contains(" ") ? signupRequest.getName().split(" ", 2)[1] : "")
                .email(signupRequest.getEmail())
                .phoneNumber(signupRequest.getPhone())
                .password(passwordEncoder.encode(signupRequest.getPassword()))
                .role(Role.USER)
                .active(true)
                .phoneVerified(false)
                .emailVerified(false)
                .build();

        @SuppressWarnings({"null", "unused"})
        User savedUser = userRepository.save(user);

        return login(createLoginRequest(signupRequest.getEmail(), signupRequest.getPassword()));
    }

    public String sendOtp(String phone) {
        // Generate 6 digit OTP
        String otp = String.format("%06d", new Random().nextInt(999999));
        otpStorage.put(phone, otp);
        otpExpiry.put(phone, LocalDateTime.now().plusMinutes(OTP_VALID_MINUTES));

        // In real app, send SMS here
        return otp;
    }

    public AuthDtos.JwtResponse verifyOtp(String phone, String otp) {
        if (!otpStorage.containsKey(phone)) {
            throw new RuntimeException("OTP not found or expired");
        }

        if (!otp.equals(otpStorage.get(phone))) {
            throw new RuntimeException("Invalid OTP");
        }

        if (LocalDateTime.now().isAfter(otpExpiry.get(phone))) {
            otpStorage.remove(phone);
            otpExpiry.remove(phone);
            throw new RuntimeException("OTP expired");
        }

        otpStorage.remove(phone);
        otpExpiry.remove(phone);

        boolean isNewUser = false;
        User user = userRepository.findByPhoneNumber(phone)
                .orElseGet(() -> {
                    // Create new user if not exists
                    User newUser = User.builder()
                            .phoneNumber(phone)
                            .role(Role.USER)
                            .active(true)
                            .phoneVerified(true)
                            .build();
                    @SuppressWarnings("null")
                    User savedNewUser = userRepository.save(newUser);
                    return savedNewUser;
                });

        if (user.getId() == null || user.getFullName() == null) {
            isNewUser = true;
        }

        // Mark phone as verified and update login time
        if (!user.isPhoneVerified()) {
            user.markPhoneAsVerified();
        }
        user.updateLastLoginTime();
        userRepository.save(user);

        // Generate token directly for OTP user
        String jwt = tokenProvider.generateTokenFromUsername(phone);

        return new AuthDtos.JwtResponse(jwt, user.getId(), user.getFullName(), user.getEmail(), user.getPhoneNumber(),
                user.getRole().name(), isNewUser);
    }

    private AuthDtos.LoginRequest createLoginRequest(String email, String password) {
        AuthDtos.LoginRequest request = new AuthDtos.LoginRequest();
        request.setEmail(email);
        request.setPassword(password);
        return request;
    }
}
