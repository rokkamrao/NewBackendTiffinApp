package com.tiffin.user.service;

import com.tiffin.user.dto.UserDto;
import com.tiffin.user.dto.UserRegistrationDto;
import com.tiffin.user.dto.UserUpdateDto;
import com.tiffin.user.model.Role;
import com.tiffin.user.model.User;
import com.tiffin.user.repository.UserRepository;
import com.tiffin.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;

    /**
     * Register a new user
     */
    public UserDto registerUser(UserRegistrationDto registrationDto) {
        log.info("Registering new user with email: {}", registrationDto.getEmail());
        
        // Check if user already exists
        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            throw new IllegalArgumentException("User with email " + registrationDto.getEmail() + " already exists");
        }
        
        if (registrationDto.getPhoneNumber() != null && 
            userRepository.existsByPhoneNumber(registrationDto.getPhoneNumber())) {
            throw new IllegalArgumentException("User with phone number " + registrationDto.getPhoneNumber() + " already exists");
        }
        
        // Create new user
        User user = User.builder()
                .email(registrationDto.getEmail())
                .password(passwordEncoder.encode(registrationDto.getPassword()))
                .firstName(registrationDto.getFirstName())
                .lastName(registrationDto.getLastName())
                .phoneNumber(registrationDto.getPhoneNumber())
                .role(Role.USER)
                .active(true)
                .emailVerified(false)
                .phoneVerified(false)
                .build();
        
        User savedUser = userRepository.save(user);
        
        // Send welcome email
        notificationService.sendWelcomeEmail(savedUser.getEmail(), savedUser.getFullName());
        
        log.info("User registered successfully with ID: {}", savedUser.getId());
        return convertToDto(savedUser);
    }

    /**
     * Find user by email
     */
    @Transactional(readOnly = true)
    public Optional<UserDto> findByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(this::convertToDto);
    }

    /**
     * Find user by ID
     */
    @Transactional(readOnly = true)
    public Optional<UserDto> findById(Long id) {
        return userRepository.findById(id)
                .map(this::convertToDto);
    }

    /**
     * Update user information
     */
    public UserDto updateUser(Long userId, UserUpdateDto updateDto) {
        log.info("Updating user with ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        
        // Validate email uniqueness if changed
        if (updateDto.getEmail() != null && !updateDto.getEmail().equals(user.getEmail())) {
            if (userRepository.findByEmailAndIdNot(updateDto.getEmail(), userId).isPresent()) {
                throw new IllegalArgumentException("Email " + updateDto.getEmail() + " is already in use");
            }
            user.setEmail(updateDto.getEmail());
            user.setEmailVerified(false); // Reset verification status
        }
        
        // Validate phone uniqueness if changed
        if (updateDto.getPhoneNumber() != null && !updateDto.getPhoneNumber().equals(user.getPhoneNumber())) {
            if (userRepository.findByPhoneNumberAndIdNot(updateDto.getPhoneNumber(), userId).isPresent()) {
                throw new IllegalArgumentException("Phone number " + updateDto.getPhoneNumber() + " is already in use");
            }
            user.setPhoneNumber(updateDto.getPhoneNumber());
            user.setPhoneVerified(false); // Reset verification status
        }
        
        // Update other fields
        if (updateDto.getFirstName() != null) {
            user.setFirstName(updateDto.getFirstName());
        }
        
        if (updateDto.getLastName() != null) {
            user.setLastName(updateDto.getLastName());
        }
        
        if (updateDto.getProfileImageUrl() != null) {
            user.setProfileImageUrl(updateDto.getProfileImageUrl());
        }
        
        if (updateDto.getPreferredLanguage() != null) {
            user.setPreferredLanguage(updateDto.getPreferredLanguage());
        }
        
        User updatedUser = userRepository.save(user);
        log.info("User updated successfully: {}", updatedUser.getId());
        
        return convertToDto(updatedUser);
    }

    /**
     * Change user password
     */
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        log.info("Changing password for user: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        log.info("Password changed successfully for user: {}", userId);
    }

    /**
     * Deactivate user account
     */
    public void deactivateUser(Long userId) {
        log.info("Deactivating user: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        
        user.setActive(false);
        userRepository.save(user);
        
        log.info("User deactivated successfully: {}", userId);
    }

    /**
     * Activate user account
     */
    public void activateUser(Long userId) {
        log.info("Activating user: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        
        user.setActive(true);
        userRepository.save(user);
        
        log.info("User activated successfully: {}", userId);
    }

    /**
     * Verify user email
     */
    public void verifyEmail(Long userId) {
        log.info("Verifying email for user: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        
        user.markEmailAsVerified();
        userRepository.save(user);
        
        log.info("Email verified successfully for user: {}", userId);
    }

    /**
     * Verify user phone
     */
    public void verifyPhone(Long userId) {
        log.info("Verifying phone for user: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        
        user.markPhoneAsVerified();
        userRepository.save(user);
        
        log.info("Phone verified successfully for user: {}", userId);
    }

    /**
     * Update last login time
     */
    public void updateLastLoginTime(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        
        user.updateLastLoginTime();
        userRepository.save(user);
    }

    /**
     * Search users
     */
    @Transactional(readOnly = true)
    public Page<UserDto> searchUsers(String searchTerm, Pageable pageable) {
        return userRepository.searchUsers(searchTerm, pageable)
                .map(this::convertToDto);
    }

    /**
     * Get users by role
     */
    @Transactional(readOnly = true)
    public Page<UserDto> getUsersByRole(Role role, Pageable pageable) {
        return userRepository.findByRole(role, pageable)
                .map(this::convertToDto);
    }

    /**
     * Get all active users
     */
    @Transactional(readOnly = true)
    public Page<UserDto> getActiveUsers(Pageable pageable) {
        return userRepository.findByActive(true, pageable)
                .map(this::convertToDto);
    }

    /**
     * Get user statistics
     */
    @Transactional(readOnly = true)
    public UserStatsDto getUserStatistics() {
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countActiveUsers();
        long verifiedUsers = userRepository.countVerifiedUsers();
        long adminUsers = userRepository.countByRole(Role.ADMIN);
        long deliveryPersons = userRepository.countByRole(Role.DELIVERY_PERSON);
        
        return UserStatsDto.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .verifiedUsers(verifiedUsers)
                .adminUsers(adminUsers)
                .deliveryPersons(deliveryPersons)
                .build();
    }

    /**
     * Convert User entity to DTO
     */
    private UserDto convertToDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .active(user.isActive())
                .emailVerified(user.isEmailVerified())
                .phoneVerified(user.isPhoneVerified())
                .profileImageUrl(user.getProfileImageUrl())
                .preferredLanguage(user.getPreferredLanguage())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .lastLoginAt(user.getLastLoginAt())
                .build();
    }

    // Inner class for user statistics
    @lombok.Data
    @lombok.Builder
    public static class UserStatsDto {
        private long totalUsers;
        private long activeUsers;
        private long verifiedUsers;
        private long adminUsers;
        private long deliveryPersons;
    }
}