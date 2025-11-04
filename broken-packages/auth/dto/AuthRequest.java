package com.tiffin.api.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthRequest {
    private String phone;
    private String email;
    private String password;
    private String name;
    private List<String> dietary;
    private List<String> allergies;
    private AddressDto address;
    private String referral;
    private Boolean newsletter;
    
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AddressDto {
        private String pincode;
        private String city;
        private String area;
    }
}