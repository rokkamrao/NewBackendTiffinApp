package com.tiffin.user.repository;

import com.tiffin.user.model.Address;
import com.tiffin.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    
    List<Address> findByUser(User user);
    
    List<Address> findByUserId(Long userId);
    
    @Query("SELECT a FROM Address a WHERE a.user.id = :userId AND a.isDefault = true")
    Optional<Address> findDefaultAddressByUserId(@Param("userId") Long userId);
    
    @Query("SELECT a FROM Address a WHERE a.user = :user AND a.isDefault = true")
    Optional<Address> findDefaultAddressByUser(@Param("user") User user);
    
    boolean existsByUserAndIsDefault(User user, boolean isDefault);
}