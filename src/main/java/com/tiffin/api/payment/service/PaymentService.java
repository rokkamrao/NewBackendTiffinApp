package com.tiffin.api.payment.service;

import com.tiffin.api.notification.service.NotificationService;
import com.tiffin.api.order.service.OrderService;
import com.tiffin.api.payment.dto.PaymentDto;
import com.tiffin.api.payment.dto.PaymentRequestDto;
import com.tiffin.api.payment.model.Payment;
import com.tiffin.api.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final OrderService orderService;
    private final NotificationService notificationService;
    private final PaymentGatewayClient paymentGatewayClient;

    /**
     * Create a Razorpay payment order WITHOUT creating our Payment record.
     * This is used for checkout flow where we create the Razorpay order first,
     * then create our Order after payment succeeds.
     */
    public PaymentDto createRazorpayOrderOnly(PaymentRequestDto paymentRequest) {
        try {
            // Process payment through payment gateway (creates Razorpay order only)
            var gatewayResponse = paymentGatewayClient.processPayment(paymentRequest);
            
            // Return minimal DTO with just the Razorpay order details (NO database save)
            return PaymentDto.builder()
                    .transactionId(gatewayResponse.getTransactionId()) // Razorpay order_id
                    .amount(paymentRequest.getAmount())
                    .currency(paymentRequest.getCurrency())
                    .status(gatewayResponse.getStatus())
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create Razorpay order: " + e.getMessage(), e);
        }
    }

    @Transactional
    public PaymentDto processPayment(String userId, PaymentRequestDto paymentRequest) {
        // Validate order and amount only if orderId is provided
        if (paymentRequest.getOrderId() != null && !paymentRequest.getOrderId().isEmpty()) {
            validateOrder(paymentRequest.getOrderId(), paymentRequest.getAmount());
        }

        // Create payment record
        Payment payment = Payment.builder()
                .id(UUID.randomUUID().toString())
                .orderId(paymentRequest.getOrderId())
                .userId(userId)
                .amount(paymentRequest.getAmount())
                .currency(paymentRequest.getCurrency())
                .status("PROCESSING")
                .paymentMethod(paymentRequest.getPaymentMethod())
                .paymentMethodDetails(maskSensitiveData(paymentRequest.getPaymentMethodDetails()))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        payment = paymentRepository.save(payment);

        try {
            // Process payment through payment gateway (creates Razorpay order)
            var gatewayResponse = paymentGatewayClient.processPayment(paymentRequest);
            
            // Update payment record with gateway response
            payment.setStatus(gatewayResponse.getStatus());
            payment.setTransactionId(gatewayResponse.getTransactionId());
            payment.setReceiptUrl(gatewayResponse.getReceiptUrl());
            
            if ("SUCCESS".equals(gatewayResponse.getStatus())) {
                // Update order status only if orderId exists
                if (payment.getOrderId() != null && !payment.getOrderId().isEmpty()) {
                    orderService.updateOrderPaymentStatus(payment.getOrderId(), "PAID");
                }
                
                // Send payment success notification only if userId exists
                if (userId != null) {
                    notificationService.sendPaymentSuccessNotification(userId, payment);
                }
            } else {
                payment.setFailureReason(gatewayResponse.getFailureReason());
                if (userId != null) {
                    notificationService.sendPaymentFailureNotification(userId, payment);
                }
            }
            
        } catch (Exception e) {
            payment.setStatus("FAILED");
            payment.setFailureReason("Payment processing error: " + e.getMessage());
            if (userId != null) {
                notificationService.sendPaymentFailureNotification(userId, payment);
            }
        }

        payment.setUpdatedAt(LocalDateTime.now());
        payment = paymentRepository.save(payment);
        
        return mapToDto(payment);
    }

    @Transactional(readOnly = true)
    public List<PaymentDto> getUserPayments(String userId) {
        return paymentRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PaymentDto getPaymentById(String paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        return mapToDto(payment);
    }

    @Transactional
    public PaymentDto retryPayment(String paymentId, PaymentRequestDto newPaymentRequest) {
        Payment oldPayment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        
        if (!"FAILED".equals(oldPayment.getStatus())) {
            throw new RuntimeException("Can only retry failed payments");
        }

        return processPayment(oldPayment.getUserId(), newPaymentRequest);
    }

    private void validateOrder(String orderId, java.math.BigDecimal amount) {
        var order = orderService.getOrderById(Long.parseLong(orderId));
        if (!order.getTotalAmount().equals(amount)) {
            throw new RuntimeException("Payment amount does not match order total");
        }
    }

    private String maskSensitiveData(Object paymentMethodDetails) {
        // Implement masking logic for sensitive payment data
        // For example: mask card numbers, etc.
        return "masked_data";
    }

    private PaymentDto mapToDto(Payment payment) {
        return PaymentDto.builder()
                .id(payment.getId())
                .orderId(payment.getOrderId())
                .userId(payment.getUserId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .paymentMethod(payment.getPaymentMethod())
                .paymentMethodDetails(payment.getPaymentMethodDetails())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .transactionId(payment.getTransactionId())
                .failureReason(payment.getFailureReason())
                .receiptUrl(payment.getReceiptUrl())
                .build();
    }

    /**
     * Update payment status using Razorpay order id (stored as transactionId) after verification/webhook.
     * @param razorpayOrderId Razorpay order id corresponding to our payment.transactionId
     * @param razorpayPaymentId Razorpay payment id (currently informational)
     * @param status New status to apply (e.g., VERIFIED, SUCCESS, FAILED)
     */
    @Transactional
    public void updatePaymentStatus(String razorpayOrderId, String razorpayPaymentId, String status) {
        paymentRepository.findFirstByTransactionId(razorpayOrderId).ifPresentOrElse(payment -> {
            payment.setStatus(status);
            payment.setUpdatedAt(LocalDateTime.now());
            // Optionally, store provider reference details in paymentMethodDetails for traceability
            String details = payment.getPaymentMethodDetails();
            String suffix = (details != null && !details.isBlank() ? ";" : "") + "rp_payment_id=" + razorpayPaymentId;
            payment.setPaymentMethodDetails((details == null ? "" : details) + suffix);
            paymentRepository.save(payment);
        }, () -> {
            // Not found: log and proceed without failing the flow
            // Using System.out avoids reliance on logger in this utility-like path
            System.out.println("Payment record not found for Razorpay order id: " + razorpayOrderId);
        });
    }
}