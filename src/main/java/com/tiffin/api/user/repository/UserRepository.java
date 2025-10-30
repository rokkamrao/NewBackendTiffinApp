package com.tiffin.api.user.repository;

import com.tiffin.api.user.model.Role;
import com.tiffin.api.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByPhone(String phone);
    Optional<User> findByEmail(String email);
    boolean existsByPhone(String phone);
    boolean existsByEmail(String email);
    
    // Role-based queries
    Page<User> findByRole(Role role, Pageable pageable);
    List<User> findByRoleAndIsActive(Role role, boolean isActive);
    long countByRole(Role role);
}