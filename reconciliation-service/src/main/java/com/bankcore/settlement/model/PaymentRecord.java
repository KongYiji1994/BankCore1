package com.bankcore.settlement.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentRecord {
    /** 内部支付指令号 */
    private String instructionId;
    /** 付款账户 */
    private String payerAccount;
    /** 收款账户 */
    private String payeeAccount;
    /** 支付金额 */
    private BigDecimal amount;
    /** 支付币种 */
    private String currency;
    /** 支付状态 */
    private String status;
    /** 创建时间 */
    private LocalDateTime createdAt;

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

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
