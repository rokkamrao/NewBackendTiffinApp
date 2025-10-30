package com.tiffin.api.payment.service;

import com.tiffin.api.payment.dto.PaymentRequestDto;

public interface PaymentGatewayClient {
    GatewayResponse processPayment(PaymentRequestDto request);

    class GatewayResponse {
        private String status;
        private String transactionId;
        private String receiptUrl;
        private String failureReason;

        public GatewayResponse(String status, String transactionId, String receiptUrl, String failureReason) {
            this.status = status;
            this.transactionId = transactionId;
            this.receiptUrl = receiptUrl;
            this.failureReason = failureReason;
        }

        public String getStatus() { return status; }
        public String getTransactionId() { return transactionId; }
        public String getReceiptUrl() { return receiptUrl; }
        public String getFailureReason() { return failureReason; }
    }
}
