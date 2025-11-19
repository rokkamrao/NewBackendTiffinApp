package com.tiffin.user.service;

import com.tiffin.user.dto.AddressDto;
import com.tiffin.user.dto.AddressRequestDto;
import com.tiffin.user.model.Address;
import com.tiffin.user.model.User;
import com.tiffin.user.repository.AddressRepository;
import com.tiffin.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    /**
     * Create a new address for a user
     */
    public AddressDto createAddress(Long userId, AddressRequestDto requestDto) {
        log.info("Creating new address for user: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + userId));
        
        // If this is set as default, unmark other default addresses
        if (requestDto.isDefault()) {
            addressRepository.unmarkOtherDefaultAddresses(userId, -1L);
        }
        
        Address address = Address.builder()
                .label(requestDto.getLabel())
                .street(requestDto.getStreet())
                .apartment(requestDto.getApartment())
                .city(requestDto.getCity())
                .state(requestDto.getState())
                .zipCode(requestDto.getZipCode())
                .country(requestDto.getCountry())
                .landmark(requestDto.getLandmark())
                .deliveryInstructions(requestDto.getDeliveryInstructions())
                .latitude(requestDto.getLatitude())
                .longitude(requestDto.getLongitude())
                .user(user)
                .isDefault(requestDto.isDefault())
                .active(true)
                .build();
        
        Address savedAddress = addressRepository.save(address);
        
        log.info("Address created successfully with ID: {}", savedAddress.getId());
        return convertToDto(savedAddress);
    }

    /**
     * Update an existing address
     */
    public AddressDto updateAddress(Long addressId, AddressRequestDto requestDto) {
        log.info("Updating address with ID: {}", addressId);
        
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new IllegalArgumentException("Address not found with ID: " + addressId));
        
        // If this is set as default, unmark other default addresses
        if (requestDto.isDefault() && !address.isDefault()) {
            addressRepository.unmarkOtherDefaultAddresses(address.getUser().getId(), addressId);
        }
        
        // Update fields
        address.setLabel(requestDto.getLabel());
        address.setStreet(requestDto.getStreet());
        address.setApartment(requestDto.getApartment());
        address.setCity(requestDto.getCity());
        address.setState(requestDto.getState());
        address.setZipCode(requestDto.getZipCode());
        address.setCountry(requestDto.getCountry());
        address.setLandmark(requestDto.getLandmark());
        address.setDeliveryInstructions(requestDto.getDeliveryInstructions());
        address.setLatitude(requestDto.getLatitude());
        address.setLongitude(requestDto.getLongitude());
        address.setDefault(requestDto.isDefault());
        
        Address updatedAddress = addressRepository.save(address);
        
        log.info("Address updated successfully: {}", updatedAddress.getId());
        return convertToDto(updatedAddress);
    }

    /**
     * Get address by ID
     */
    @Transactional(readOnly = true)
    public Optional<AddressDto> getAddressById(Long addressId) {
        return addressRepository.findById(addressId)
                .map(this::convertToDto);
    }

    /**
     * Get all addresses for a user
     */
    @Transactional(readOnly = true)
    public List<AddressDto> getUserAddresses(Long userId) {
        return addressRepository.findByUserIdAndActive(userId, true)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get addresses with pagination
     */
    @Transactional(readOnly = true)
    public Page<AddressDto> getUserAddresses(Long userId, Pageable pageable) {
        return addressRepository.findByUserId(userId, pageable)
                .map(this::convertToDto);
    }

    /**
     * Get user's default address
     */
    @Transactional(readOnly = true)
    public Optional<AddressDto> getDefaultAddress(Long userId) {
        return addressRepository.findDefaultAddressByUserId(userId)
                .map(this::convertToDto);
    }

    /**
     * Set address as default
     */
    public void setAsDefault(Long addressId) {
        log.info("Setting address as default: {}", addressId);
        
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new IllegalArgumentException("Address not found with ID: " + addressId));
        
        // Unmark other default addresses for this user
        addressRepository.unmarkOtherDefaultAddresses(address.getUser().getId(), addressId);
        
        // Mark this address as default
        address.markAsDefault();
        addressRepository.save(address);
        
        log.info("Address set as default successfully: {}", addressId);
    }

    /**
     * Delete an address
     */
    public void deleteAddress(Long addressId) {
        log.info("Deleting address: {}", addressId);
        
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new IllegalArgumentException("Address not found with ID: " + addressId));
        
        // Soft delete
        address.deactivate();
        addressRepository.save(address);
        
        log.info("Address deleted successfully: {}", addressId);
    }

    /**
     * Hard delete an address
     */
    public void hardDeleteAddress(Long addressId) {
        log.info("Hard deleting address: {}", addressId);
        
        if (!addressRepository.existsById(addressId)) {
            throw new IllegalArgumentException("Address not found with ID: " + addressId);
        }
        
        addressRepository.deleteById(addressId);
        log.info("Address hard deleted successfully: {}", addressId);
    }

    /**
     * Search addresses within radius
     */
    @Transactional(readOnly = true)
    public List<AddressDto> findAddressesWithinRadius(Double latitude, Double longitude, Double radiusKm) {
        return addressRepository.findAddressesWithinRadius(latitude, longitude, radiusKm)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Search user addresses
     */
    @Transactional(readOnly = true)
    public List<AddressDto> searchUserAddresses(Long userId, String searchTerm) {
        return addressRepository.searchUserAddresses(userId, searchTerm)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get addresses by city
     */
    @Transactional(readOnly = true)
    public List<AddressDto> getAddressesByCity(String city) {
        return addressRepository.findByCityAndActive(city, true)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get all active cities
     */
    @Transactional(readOnly = true)
    public List<String> getAllActiveCities() {
        return addressRepository.findAllActiveCities();
    }

    /**
     * Get all active states
     */
    @Transactional(readOnly = true)
    public List<String> getAllActiveStates() {
        return addressRepository.findAllActiveStates();
    }

    /**
     * Get ZIP codes by city
     */
    @Transactional(readOnly = true)
    public List<String> getZipCodesByCity(String city) {
        return addressRepository.findZipCodesByCity(city);
    }

    /**
     * Update address coordinates
     */
    public void updateAddressCoordinates(Long addressId, Double latitude, Double longitude) {
        log.info("Updating coordinates for address: {}", addressId);
        
        if (!addressRepository.existsById(addressId)) {
            throw new IllegalArgumentException("Address not found with ID: " + addressId);
        }
        
        addressRepository.updateCoordinates(addressId, latitude, longitude);
        log.info("Address coordinates updated successfully: {}", addressId);
    }

    /**
     * Validate address for delivery
     */
    @Transactional(readOnly = true)
    public boolean isValidForDelivery(Long addressId) {
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new IllegalArgumentException("Address not found with ID: " + addressId));
        
        return address.isValidForDelivery();
    }

    /**
     * Get address statistics
     */
    @Transactional(readOnly = true)
    public AddressStatsDto getAddressStatistics() {
        long totalAddresses = addressRepository.count();
        long activeAddresses = addressRepository.countActiveAddressesByUserId(null);
        long addressesWithCoordinates = addressRepository.findAddressesWithCoordinates().size();
        long incompleteAddresses = addressRepository.countIncompleteAddresses();
        
        return AddressStatsDto.builder()
                .totalAddresses(totalAddresses)
                .activeAddresses(activeAddresses)
                .addressesWithCoordinates(addressesWithCoordinates)
                .incompleteAddresses(incompleteAddresses)
                .build();
    }

    /**
     * Convert Address entity to DTO
     */
    private AddressDto convertToDto(Address address) {
        return AddressDto.builder()
                .id(address.getId())
                .label(address.getLabel())
                .street(address.getStreet())
                .apartment(address.getApartment())
                .city(address.getCity())
                .state(address.getState())
                .zipCode(address.getZipCode())
                .country(address.getCountry())
                .landmark(address.getLandmark())
                .deliveryInstructions(address.getDeliveryInstructions())
                .latitude(address.getLatitude())
                .longitude(address.getLongitude())
                .userId(address.getUser().getId())
                .isDefault(address.isDefault())
                .active(address.isActive())
                .createdAt(address.getCreatedAt())
                .updatedAt(address.getUpdatedAt())
                .build();
    }

    // Inner class for address statistics
    @lombok.Data
    @lombok.Builder
    public static class AddressStatsDto {
        private long totalAddresses;
        private long activeAddresses;
        private long addressesWithCoordinates;
        private long incompleteAddresses;
    }
}