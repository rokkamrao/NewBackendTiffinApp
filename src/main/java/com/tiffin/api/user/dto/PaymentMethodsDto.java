package com.tiffin.api.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethodsDto {
    private List<SavedCardDto> cards;
    private List<String> upiIds;
    private String preferredMethod;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class SavedCardDto {
    private String id;
    private String last4;
    private String brand;
    private String holderName;
    private String expiryMonth;
    private String expiryYear;
}