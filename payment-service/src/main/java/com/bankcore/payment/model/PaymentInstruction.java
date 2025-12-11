package com.bankcore.payment.model;

import com.bankcore.common.dto.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentInstruction {
    private String requestId;
    private String instructionId;
    private String payerAccount;
    private String payeeAccount;
    private String payerCustomerId;
    private String payerCustomerStatus;
    private String currency;
    private BigDecimal amount;
    private String purpose;
    private String channel;
    private String batchId;
    private Integer priority;
    private BigDecimal riskScore;
    private PaymentStatus status;
    private LocalDateTime createdAt;

    public PaymentInstruction() {
    }

    public PaymentInstruction(String requestId, String instructionId, String payerAccount, String payeeAccount, String payerCustomerId, String payerCustomerStatus, String currency, BigDecimal amount, String purpose, String channel, String batchId, Integer priority, PaymentStatus initialStatus) {
        this.requestId = requestId;
        this.instructionId = instructionId;
        this.payerAccount = payerAccount;
        this.payeeAccount = payeeAccount;
        this.payerCustomerId = payerCustomerId;
        this.payerCustomerStatus = payerCustomerStatus;
        this.currency = currency;
        this.amount = amount;
        this.purpose = purpose;
        this.channel = channel;
        this.batchId = batchId;
        this.priority = priority == null ? 5 : priority;
        this.status = initialStatus == null ? PaymentStatus.PENDING : initialStatus;
        this.riskScore = BigDecimal.ZERO;
        this.createdAt = LocalDateTime.now();
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getInstructionId() {
        return instructionId;
    }

    public void setInstructionId(String instructionId) {
        this.instructionId = instructionId;
    }

    public String getPayerAccount() {
        return payerAccount;
    }

    public void setPayerAccount(String payerAccount) {
        this.payerAccount = payerAccount;
    }

    public String getPayeeAccount() {
        return payeeAccount;
    }

    public void setPayeeAccount(String payeeAccount) {
        this.payeeAccount = payeeAccount;
    }

    public String getPayerCustomerId() {
        return payerCustomerId;
    }

    public void setPayerCustomerId(String payerCustomerId) {
        this.payerCustomerId = payerCustomerId;
    }

    public String getPayerCustomerStatus() {
        return payerCustomerStatus;
    }

    public void setPayerCustomerStatus(String payerCustomerStatus) {
        this.payerCustomerStatus = payerCustomerStatus;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public BigDecimal getRiskScore() {
        return riskScore;
    }

    public void setRiskScore(BigDecimal riskScore) {
        this.riskScore = riskScore;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
