package com.tiffin.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDto {
    private String id;
    private String orderId;
    private String userId;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String paymentMethod;
    private String paymentMethodDetails;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String transactionId;
    private String failureReason;
    private String receiptUrl;
}