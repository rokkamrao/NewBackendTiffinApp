package com.tiffin.api.user.service;

import com.tiffin.api.user.dto.AddressDto;
import com.tiffin.api.user.dto.UserProfileDto;
import com.tiffin.api.user.model.Address;
import com.tiffin.api.user.model.User;
import com.tiffin.api.user.repository.AddressRepository;
import com.tiffin.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import com.tiffin.api.storage.FileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserProfileService {
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final FileStorageService fileStorageService;

    @Transactional(readOnly = true)
    public UserProfileDto getUserProfile(String userId) {
        User user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new RuntimeException("User not found"));
        return mapToDto(user);
    }

    @Transactional
    public UserProfileDto updateProfile(String userId, UserProfileDto profileDto) {
        User user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setName(profileDto.getName());
        user.setEmail(profileDto.getEmail());
        user.setDietaryPreferences(profileDto.getDietaryPreferences());
        user.setPreferredLanguage(profileDto.getPreferredLanguage());
        user.setNotificationsEnabled(profileDto.isNotificationsEnabled());

        User updatedUser = userRepository.save(user);
        return mapToDto(updatedUser);
    }

    @Transactional
    public String uploadAvatar(String userId, MultipartFile file) {
        User user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new RuntimeException("User not found"));

    String avatarUrl = fileStorageService.storeFile(file, "avatars/" + userId);
        user.setAvatarUrl(avatarUrl);
        userRepository.save(user);

        return avatarUrl;
    }

    @Transactional
    public AddressDto addAddress(String userId, AddressDto addressDto) {
        User user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new RuntimeException("User not found"));

        Address address = Address.builder()
                .user(user)
                .type(addressDto.getType())
                .addressLine1(addressDto.getAddressLine1())
                .addressLine2(addressDto.getAddressLine2())
                .landmark(addressDto.getLandmark())
                .city(addressDto.getCity())
                .state(addressDto.getState())
                .pincode(addressDto.getPincode())
                .latitude(addressDto.getLatitude())
                .longitude(addressDto.getLongitude())
                .isDefault(addressDto.isDefault())
                .label(addressDto.getLabel())
                .build();

        if (addressDto.isDefault()) {
            // Update other addresses to non-default
            addressRepository.updateAllAddressesToNonDefault(user.getId());
        }

        Address savedAddress = addressRepository.save(address);
        return mapToAddressDto(savedAddress);
    }

    @Transactional
    public void deleteAddress(String userId, Long addressId) {
        Address address = addressRepository.findByIdAndUserId(addressId, Long.parseLong(userId))
                .orElseThrow(() -> new RuntimeException("Address not found"));
        addressRepository.delete(address);
    }

    @Transactional
    public void setDefaultAddress(String userId, Long addressId) {
        Long uid = Long.parseLong(userId);
        addressRepository.updateAllAddressesToNonDefault(uid);
        Address address = addressRepository.findByIdAndUserId(addressId, uid)
                .orElseThrow(() -> new RuntimeException("Address not found"));
        address.setDefault(true);
        addressRepository.save(address);
    }

    private UserProfileDto mapToDto(User user) {
        List<AddressDto> addressDtos = user.getAddresses().stream()
                .map(this::mapToAddressDto)
                .collect(Collectors.toList());

    return UserProfileDto.builder()
        .id(String.valueOf(user.getId()))
                .phone(user.getPhone())
                .name(user.getName())
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .addresses(addressDtos)
                .dietaryPreferences(user.getDietaryPreferences())
                .preferredLanguage(user.getPreferredLanguage())
                .referralCode(user.getReferralCode())
                .notificationsEnabled(user.isNotificationsEnabled())
        .savedPaymentMethods(com.tiffin.api.user.dto.PaymentMethodsDto.builder()
            .upiIds(user.getSavedPaymentMethods() != null ? new java.util.ArrayList<>(user.getSavedPaymentMethods()) : null)
            .build())
                .build();
    }

    private AddressDto mapToAddressDto(Address address) {
        return AddressDto.builder()
                .id(address.getId())
                .type(address.getType())
                .addressLine1(address.getAddressLine1())
                .addressLine2(address.getAddressLine2())
                .landmark(address.getLandmark())
                .city(address.getCity())
                .state(address.getState())
                .pincode(address.getPincode())
                .latitude(address.getLatitude())
                .longitude(address.getLongitude())
                .isDefault(address.isDefault())
                .label(address.getLabel())
                .build();
    }
}