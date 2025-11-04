package com.tiffin.user.repository;

import com.tiffin.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByPhoneNumber(String phoneNumber);
    
    boolean existsByEmail(String email);
    
    boolean existsByPhoneNumber(String phoneNumber);
    
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.id != :id")
    Optional<User> findByEmailAndIdNot(@Param("email") String email, @Param("id") Long id);
    
    @Query("SELECT u FROM User u WHERE u.phoneNumber = :phoneNumber AND u.id != :id")
    Optional<User> findByPhoneNumberAndIdNot(@Param("phoneNumber") String phoneNumber, @Param("id") Long id);
}