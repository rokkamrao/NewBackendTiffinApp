package com.tiffin.api.payment.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
@Entity
@Table(name = "payments")
public class Payment {
    @Id
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
