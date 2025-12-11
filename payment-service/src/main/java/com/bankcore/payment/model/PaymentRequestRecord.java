package com.bankcore.payment.model;

import java.time.LocalDateTime;

public class PaymentRequestRecord {
    private String requestId;
    private String paymentInstructionId;
    private PaymentRequestStatus status;
    private String message;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PaymentRequestRecord() {
    }

    public PaymentRequestRecord(String requestId, String paymentInstructionId, PaymentRequestStatus status) {
        this.requestId = requestId;
        this.paymentInstructionId = paymentInstructionId;
        this.status = status;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getPaymentInstructionId() {
        return paymentInstructionId;
    }

    public void setPaymentInstructionId(String paymentInstructionId) {
        this.paymentInstructionId = paymentInstructionId;
    }

    public PaymentRequestStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentRequestStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
