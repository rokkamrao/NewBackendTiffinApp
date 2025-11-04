package com.tiffin.api.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerifyOtpRequest {
    private String phone;
    private String otp;
    
    // Manual getters for compatibility
    public String getPhone() {
        return phone;
    }
    
    public String getOtp() {
        return otp;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public void setOtp(String otp) {
        this.otp = otp;
    }
}