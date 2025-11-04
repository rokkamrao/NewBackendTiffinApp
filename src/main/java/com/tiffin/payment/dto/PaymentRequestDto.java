package com.tiffin.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestDto {
    private String orderId;
    private BigDecimal amount;
    private String currency;
    private String paymentMethod;
    private PaymentMethodDetails paymentMethodDetails;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class PaymentMethodDetails {
    // For Card payments
    private String cardNumber;
    private String expiryMonth;
    private String expiryYear;
    private String cvv;
    private String cardHolderName;
    
    // For UPI payments
    private String upiId;
    
    // For Wallet payments
    private String walletProvider;
    private String walletId;
}