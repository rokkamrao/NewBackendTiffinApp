package com.tiffin.api.payment.controller;

import com.razorpay.Payment;
import com.razorpay.RazorpayException;
import com.tiffin.api.payment.dto.PaymentDto;
import com.tiffin.api.payment.dto.PaymentRequestDto;
import com.tiffin.api.payment.service.PaymentService;
import com.tiffin.api.payment.service.RazorpayPaymentGatewayClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final RazorpayPaymentGatewayClient razorpayClient;

    @Value("${app.razorpay.webhook-secret}")
    private String webhookSecret;

    /**
     * Create a new payment order
     */
    @PostMapping
    public ResponseEntity<PaymentDto> createPayment(
            @RequestBody PaymentRequestDto paymentRequest,
            Authentication authentication) {
        // For anonymous checkout (no orderId yet), just create Razorpay order
        if (paymentRequest.getOrderId() == null || paymentRequest.getOrderId().isEmpty()) {
            PaymentDto payment = paymentService.createRazorpayOrderOnly(paymentRequest);
            return ResponseEntity.ok(payment);
        }
        
        // For authenticated users with existing order, process full payment
        String userId = (authentication != null) ? authentication.getName() : null;
        PaymentDto payment = paymentService.processPayment(userId, paymentRequest);
        return ResponseEntity.ok(payment);
    }

    /**
     * Get all payments for the authenticated user
     */
    @GetMapping
    public ResponseEntity<List<PaymentDto>> getUserPayments(Authentication authentication) {
        String userId = authentication.getName();
        List<PaymentDto> payments = paymentService.getUserPayments(userId);
        return ResponseEntity.ok(payments);
    }

    /**
     * Get specific payment by ID
     */
    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentDto> getPayment(@PathVariable String paymentId) {
        PaymentDto payment = paymentService.getPaymentById(paymentId);
        return ResponseEntity.ok(payment);
    }

    /**
     * Verify payment after completion
     */
    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyPayment(
            @RequestBody Map<String, String> verificationData) {
        
        String razorpayOrderId = verificationData.get("razorpay_order_id");
        String razorpayPaymentId = verificationData.get("razorpay_payment_id");
        String razorpaySignature = verificationData.get("razorpay_signature");

        boolean isValid = razorpayClient.verifyPaymentSignature(
                razorpayOrderId, razorpayPaymentId, razorpaySignature);

        if (isValid) {
            try {
                // Fetch payment details from Razorpay and log for traceability
                Payment payment = razorpayClient.fetchPayment(razorpayPaymentId);
                log.debug("Razorpay payment details: {}", payment);

                // Update payment status in database (e.g., VERIFIED)
                paymentService.updatePaymentStatus(razorpayOrderId, razorpayPaymentId, "VERIFIED");
                log.info("Payment verified successfully: {}", razorpayPaymentId);

                return ResponseEntity.ok(Map.of(
                        "status", "success",
                        "message", "Payment verified successfully",
                        "payment_id", razorpayPaymentId
                ));
            } catch (RazorpayException e) {
                log.error("Failed to fetch payment details", e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("status", "error", "message", e.getMessage()));
            }
        } else {
            log.warn("Payment signature verification failed for order: {}", razorpayOrderId);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("status", "failed", "message", "Invalid signature"));
        }
    }

    /**
     * Razorpay webhook handler
     */
    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("X-Razorpay-Signature") String signature) {
        
        try {
            // Verify webhook signature
            if (!verifyWebhookSignature(payload, signature)) {
                log.warn("Invalid webhook signature");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid signature");
            }

            JSONObject webhook = new JSONObject(payload);
            String event = webhook.getString("event");
            JSONObject payloadData = webhook.getJSONObject("payload");
            JSONObject paymentEntity = payloadData.getJSONObject("payment").getJSONObject("entity");

            log.info("Webhook received: event={}, payment_id={}", event, paymentEntity.getString("id"));

            // Handle different events
            switch (event) {
                case "payment.authorized":
                    handlePaymentAuthorized(paymentEntity);
                    break;
                case "payment.captured":
                    handlePaymentCaptured(paymentEntity);
                    break;
                case "payment.failed":
                    handlePaymentFailed(paymentEntity);
                    break;
                case "order.paid":
                    handleOrderPaid(payloadData.getJSONObject("order").getJSONObject("entity"));
                    break;
                default:
                    log.info("Unhandled webhook event: {}", event);
            }

            return ResponseEntity.ok("Webhook processed");
            
        } catch (Exception e) {
            log.error("Error processing webhook", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Webhook processing failed");
        }
    }

    /**
     * Retry failed payment
     */
    @PostMapping("/{paymentId}/retry")
    public ResponseEntity<PaymentDto> retryPayment(
            @PathVariable String paymentId,
            @RequestBody PaymentRequestDto newPaymentRequest) {
        
        PaymentDto payment = paymentService.retryPayment(paymentId, newPaymentRequest);
        return ResponseEntity.ok(payment);
    }

    // Helper methods for webhook handling

    private boolean verifyWebhookSignature(String payload, String signature) {
        try {
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(webhookSecret.getBytes(), "HmacSHA256");
            sha256Hmac.init(secretKey);
            
            byte[] hash = sha256Hmac.doFinal(payload.getBytes());
            String computedSignature = Base64.getEncoder().encodeToString(hash);
            
            return computedSignature.equals(signature);
        } catch (Exception e) {
            log.error("Error verifying webhook signature", e);
            return false;
        }
    }

    private void handlePaymentAuthorized(JSONObject payment) {
        String paymentId = payment.getString("id");
        String orderId = payment.getJSONObject("notes").getString("order_id");
        log.info("Payment authorized: payment_id={}, order_id={}", paymentId, orderId);
        // Update payment status to AUTHORIZED in database
    }

    private void handlePaymentCaptured(JSONObject payment) {
        String paymentId = payment.getString("id");
        String orderId = payment.getJSONObject("notes").getString("order_id");
        log.info("Payment captured: payment_id={}, order_id={}", paymentId, orderId);
        // Update payment status to SUCCESS in database
        // Update order status to PAID
    }

    private void handlePaymentFailed(JSONObject payment) {
        String paymentId = payment.getString("id");
        String orderId = payment.getJSONObject("notes").optString("order_id", "");
        String errorCode = payment.optString("error_code", "");
        String errorDescription = payment.optString("error_description", "");

        log.warn("Payment failed: payment_id={}, order_id={}, code={}, error={}",
                paymentId, orderId, errorCode, errorDescription);
        // Update payment status to FAILED in database
        // Send notification to user
    }

    private void handleOrderPaid(JSONObject order) {
        String orderId = order.getJSONObject("notes").getString("order_id");
        log.info("Order paid: order_id={}", orderId);
        // Update order status to PAID
        // Trigger fulfillment workflow
    }
}
