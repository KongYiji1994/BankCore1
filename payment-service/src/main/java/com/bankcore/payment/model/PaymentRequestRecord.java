package com.bankcore.payment.model;

import java.time.LocalDateTime;

public class PaymentRequestRecord {
    /** 请求ID */
    private String requestId;
    /** 关联的支付指令ID */
    private String paymentInstructionId;
    /** 请求处理状态 */
    private PaymentRequestStatus status;
    /** 状态描述或错误信息 */
    private String message;
    /** 创建时间 */
    private LocalDateTime createdAt;
    /** 最近更新时间 */
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
