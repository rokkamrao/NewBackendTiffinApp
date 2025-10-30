package com.tiffin.api.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDto {
    private String id;
    private String phone;
    private String name;
    private String email;
    private String avatarUrl;
    private List<AddressDto> addresses;
    private Set<String> dietaryPreferences;
    private String preferredLanguage;
    private String referralCode;
    private boolean notificationsEnabled;
    private PaymentMethodsDto savedPaymentMethods;
}