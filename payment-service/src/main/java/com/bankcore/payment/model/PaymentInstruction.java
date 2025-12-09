package com.bankcore.payment.model;

import com.bankcore.common.dto.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentInstruction {
    private String instructionId;
    private String payerAccount;
    private String payeeAccount;
    private String currency;
    private BigDecimal amount;
    private String purpose;
    private PaymentStatus status;
    private LocalDateTime createdAt;

    public PaymentInstruction() {
    }

    public PaymentInstruction(String instructionId, String payerAccount, String payeeAccount, String currency, BigDecimal amount, String purpose) {
        this.instructionId = instructionId;
        this.payerAccount = payerAccount;
        this.payeeAccount = payeeAccount;
        this.currency = currency;
        this.amount = amount;
        this.purpose = purpose;
        this.status = PaymentStatus.INITIATED;
        this.createdAt = LocalDateTime.now();
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
