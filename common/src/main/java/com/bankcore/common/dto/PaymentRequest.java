package com.bankcore.common.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

public class PaymentRequest {
    @NotBlank
    private String requestId;
    @NotBlank
    private String instructionId;
    @NotBlank
    private String payerAccount;
    @NotBlank
    private String payeeAccount;
    @NotBlank
    private String currency;
    @NotNull
    @Min(1)
    private BigDecimal amount;
    @NotBlank
    private String purpose;

    private String channel;

    private String batchId;

    private Integer priority;

    public PaymentRequest() {
    }

    public PaymentRequest(String requestId, String instructionId, String payerAccount, String payeeAccount, String currency, BigDecimal amount, String purpose) {
        this.requestId = requestId;
        this.instructionId = instructionId;
        this.payerAccount = payerAccount;
        this.payeeAccount = payeeAccount;
        this.currency = currency;
        this.amount = amount;
        this.purpose = purpose;
    }

    public PaymentRequest(String requestId, String instructionId, String payerAccount, String payeeAccount, String currency, BigDecimal amount, String purpose, String channel, String batchId, Integer priority) {
        this.requestId = requestId;
        this.instructionId = instructionId;
        this.payerAccount = payerAccount;
        this.payeeAccount = payeeAccount;
        this.currency = currency;
        this.amount = amount;
        this.purpose = purpose;
        this.channel = channel;
        this.batchId = batchId;
        this.priority = priority;
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
}
