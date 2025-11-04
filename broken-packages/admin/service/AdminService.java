package com.tiffin.api.admin.service;

import com.tiffin.api.admin.dto.CreateAdminUserRequest;
import com.tiffin.api.admin.dto.CreateDeliveryUserRequest;
import com.tiffin.api.admin.dto.UserManagementDto;
import com.tiffin.api.user.model.Role;
import com.tiffin.api.user.model.User;
import com.tiffin.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {
    
    private static final Logger log = LoggerFactory.getLogger(AdminService.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Transactional
    public UserManagementDto createDeliveryUser(CreateDeliveryUserRequest request) {
        log.info("🚚 Admin creating delivery user with phone: {}", request.getPhone());
        
        // Check if phone already exists
        if (userRepository.findByPhone(request.getPhone()).isPresent()) {
            log.error("❌ Phone number already exists: {}", request.getPhone());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Phone number already exists");
        }
        
        // Create delivery user
        User deliveryUser = User.builder()
                .name(request.getName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.DELIVERY_USER)
                .isActive(true)
                .avatarUrl(request.getAvatarUrl())
                .build();
        
        User savedUser = userRepository.save(deliveryUser);
        log.info("✅ Delivery user created successfully with ID: {}", savedUser.getId());
        
        return mapToUserManagementDto(savedUser);
    }
    
    @Transactional
    public UserManagementDto createAdminUser(CreateAdminUserRequest request) {
        log.info("👑 Creating admin user with phone: {}", request.getPhone());
        
        // Check if phone already exists
        if (userRepository.findByPhone(request.getPhone()).isPresent()) {
            log.error("❌ Phone number already exists: {}", request.getPhone());
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Phone number already exists");
        }
        
        // Create admin user
        User adminUser = User.builder()
                .name(request.getName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ADMIN)
                .isActive(true)
                .build();
        
        User savedUser = userRepository.save(adminUser);
        log.info("✅ Admin user created successfully with ID: {}", savedUser.getId());
        
        return mapToUserManagementDto(savedUser);
    }
    
    @Transactional
    public UserManagementDto toggleUserActivation(Long userId) {
        log.info("🔄 Toggling activation status for user ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("❌ User not found with ID: {}", userId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
                });
        
        user.setActive(!user.isActive());
        User savedUser = userRepository.save(user);
        
        log.info("✅ User {} is now {}", userId, savedUser.isActive() ? "ACTIVE" : "INACTIVE");
        
        return mapToUserManagementDto(savedUser);
    }
    
    public Page<UserManagementDto> getAllDeliveryUsers(Pageable pageable) {
        log.info("📋 Fetching all delivery users");
        return userRepository.findByRole(Role.DELIVERY_USER, pageable)
                .map(this::mapToUserManagementDto);
    }
    
    public Page<UserManagementDto> getAllAdminUsers(Pageable pageable) {
        log.info("📋 Fetching all admin users");
        return userRepository.findByRole(Role.ADMIN, pageable)
                .map(this::mapToUserManagementDto);
    }
    
    public Page<UserManagementDto> getAllCustomers(Pageable pageable) {
        log.info("📋 Fetching all customers");
        return userRepository.findByRole(Role.CUSTOMER, pageable)
                .map(this::mapToUserManagementDto);
    }
    
    public List<UserManagementDto> getActiveDeliveryUsers() {
        log.info("📋 Fetching active delivery users");
        return userRepository.findByRoleAndIsActive(Role.DELIVERY_USER, true)
                .stream()
                .map(this::mapToUserManagementDto)
                .collect(Collectors.toList());
    }
    
    public UserManagementDto getUserById(Long userId) {
        log.info("🔍 Fetching user by ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("❌ User not found with ID: {}", userId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
                });
        
        return mapToUserManagementDto(user);
    }
    
    @Transactional
    public void deleteUser(Long userId) {
        log.info("🗑️ Deleting user with ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("❌ User not found with ID: {}", userId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
                });
        
        userRepository.delete(user);
        log.info("✅ User deleted successfully: {}", userId);
    }
    
    private UserManagementDto mapToUserManagementDto(User user) {
        return UserManagementDto.builder()
                .id(user.getId())
                .name(user.getName())
                .phone(user.getPhone())
                .email(user.getEmail())
                .role(user.getRole())
                .isActive(user.isActive())
                .avatarUrl(user.getAvatarUrl())
                .build();
    }
}
