package com.tiffin.api.config;

import com.tiffin.api.user.model.Role;
import com.tiffin.api.user.model.User;
import com.tiffin.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Order(1) // Run before other initializers
@RequiredArgsConstructor
public class AdminInitializer implements CommandLineRunner {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) {
        initializeDefaultAdmin();
    }
    
    private void initializeDefaultAdmin() {
        log.info("👑 Checking for default admin user...");
        
        // Check if any admin user exists
        if (userRepository.findByRoleAndIsActive(Role.ADMIN, true).isEmpty()) {
            log.info("🔧 No admin user found. Creating default admin...");
            
            User defaultAdmin = User.builder()
                    .name("Super Admin")
                    .phone("9999999999")
                    .email("admin@tiffin.com")
                    .password(passwordEncoder.encode("admin@123"))
                    .role(Role.ADMIN)
                    .isActive(true)
                    .build();
            
            userRepository.save(defaultAdmin);
            
            log.info("✅ Default admin user created successfully!");
            log.info("📱 Phone: 9999999999");
            log.info("📧 Email: admin@tiffin.com");
            log.info("🔑 Password: admin@123");
            log.info("⚠️  PLEASE CHANGE THE DEFAULT PASSWORD AFTER FIRST LOGIN!");
        } else {
            log.info("✅ Admin user already exists. Skipping initialization.");
        }
    }
}
