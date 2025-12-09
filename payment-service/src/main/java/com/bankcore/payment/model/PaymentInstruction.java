package com.bankcore.payment.model;

import com.bankcore.common.dto.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;

public class PaymentInstruction {
    private final String instructionId;
    private final String payerAccount;
    private final String payeeAccount;
    private final String currency;
    private final BigDecimal amount;
    private final String purpose;
    private PaymentStatus status;
    private final Instant createdAt;

    public PaymentInstruction(String instructionId, String payerAccount, String payeeAccount, String currency, BigDecimal amount, String purpose) {
        this.instructionId = instructionId;
        this.payerAccount = payerAccount;
        this.payeeAccount = payeeAccount;
        this.currency = currency;
        this.amount = amount;
        this.purpose = purpose;
        this.status = PaymentStatus.INITIATED;
        this.createdAt = Instant.now();
    }

    public String getInstructionId() {
        return instructionId;
    }

    public String getPayerAccount() {
        return payerAccount;
    }

    public String getPayeeAccount() {
        return payeeAccount;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getPurpose() {
        return purpose;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
