package com.tiffin.api.auth.security;

import com.tiffin.api.user.model.Role;
import com.tiffin.api.user.model.User;
import com.tiffin.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RoleCheckAspect {
    
    private final UserRepository userRepository;
    
    @Before("@annotation(requireRole)")
    public void checkRole(RequireRole requireRole) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            log.error("❌ Unauthorized access attempt - No authentication");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }
        
        String phone = authentication.getName();
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> {
                    log.error("❌ User not found: {}", phone);
                    return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found");
                });
        
        Role[] allowedRoles = requireRole.value();
        boolean hasRole = Arrays.asList(allowedRoles).contains(user.getRole());
        
        if (!hasRole) {
            log.error("❌ Access denied for user {} with role {}. Required roles: {}", 
                     phone, user.getRole(), Arrays.toString(allowedRoles));
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
                "Access denied. Required role: " + Arrays.toString(allowedRoles));
        }
        
        log.info("✅ Access granted for user {} with role {}", phone, user.getRole());
    }
}
