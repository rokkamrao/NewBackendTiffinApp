package com.tiffin.payment.service;

import com.razorpay.Order;
import com.razorpay.Payment;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.tiffin.payment.dto.PaymentRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class RazorpayPaymentGatewayClient implements PaymentGatewayClient {
    
    private final RazorpayClient razorpayClient;
    
    @Value("${app.razorpay.key-secret}")
    private String keySecret;

    @Override
    public GatewayResponse processPayment(PaymentRequestDto request) {
        try {
            // Create Razorpay Order
            JSONObject orderRequest = new JSONObject();
            // Razorpay expects amount in paise (multiply by 100)
            orderRequest.put("amount", request.getAmount().multiply(BigDecimal.valueOf(100)).intValue());
            orderRequest.put("currency", request.getCurrency() != null ? request.getCurrency() : "INR");
            orderRequest.put("receipt", "order_" + request.getOrderId());
            
            JSONObject notes = new JSONObject();
            notes.put("order_id", request.getOrderId());
            notes.put("payment_method", request.getPaymentMethod());
            orderRequest.put("notes", notes);

            Order razorpayOrder = razorpayClient.orders.create(orderRequest);
            
            String razorpayOrderId = razorpayOrder.get("id");
            String status = razorpayOrder.get("status");
            
            log.info("Razorpay order created: orderId={}, status={}, amount={}", 
                    razorpayOrderId, status, razorpayOrder.get("amount"));

            // In test mode, orders are created but payments need to be completed via Razorpay UI
            // For now, return the order details
            // In production, you'd return razorpayOrderId to frontend to complete payment
            
            return new GatewayResponse(
                    "PENDING", // Payment is pending until user completes it
                    razorpayOrderId,
                    null, // Receipt URL will be available after payment completion
                    null
            );
            
        } catch (RazorpayException e) {
            log.error("Razorpay payment failed for order: {}", request.getOrderId(), e);
            return new GatewayResponse(
                    "FAILED",
                    null,
                    null,
                    e.getMessage()
            );
        }
    }

    /**
     * Verify payment signature (to be called from webhook or payment completion callback)
     */
    public boolean verifyPaymentSignature(String razorpayOrderId, String razorpayPaymentId, String signature) {
        try {
            JSONObject attributes = new JSONObject();
            attributes.put("razorpay_order_id", razorpayOrderId);
            attributes.put("razorpay_payment_id", razorpayPaymentId);
            attributes.put("razorpay_signature", signature);
            
            return com.razorpay.Utils.verifyPaymentSignature(attributes, keySecret);
        } catch (RazorpayException e) {
            log.error("Payment signature verification failed", e);
            return false;
        }
    }

    /**
     * Fetch payment details from Razorpay
     */
    public Payment fetchPayment(String paymentId) throws RazorpayException {
        return razorpayClient.payments.fetch(paymentId);
    }

    /**
     * Capture payment (for authorized payments)
     */
    public Payment capturePayment(String paymentId, BigDecimal amount) throws RazorpayException {
        JSONObject captureRequest = new JSONObject();
        captureRequest.put("amount", amount.multiply(BigDecimal.valueOf(100)).intValue());
        return razorpayClient.payments.capture(paymentId, captureRequest);
    }

    /**
     * Refund payment
     */
    public void refundPayment(String paymentId, BigDecimal amount) throws RazorpayException {
        JSONObject refundRequest = new JSONObject();
        if (amount != null) {
            refundRequest.put("amount", amount.multiply(BigDecimal.valueOf(100)).intValue());
        }
        razorpayClient.payments.refund(paymentId, refundRequest);
    }
}
