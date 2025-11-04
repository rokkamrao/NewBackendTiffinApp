package com.tiffin.api.admin.dto;

import com.tiffin.api.user.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserManagementDto {
    private Long id;
    private String name;
    private String phone;
    private String email;
    private Role role;
    private boolean isActive;
    private String avatarUrl;
    private String vehicleNumber;
    private String licenseNumber;
}
