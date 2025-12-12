package com.bankcore.account.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 账户流水：用于实现请求级幂等，防止重复的冻结/扣款/解冻等操作被执行多次。
 */
public class AccountLedgerEntry {
    private String entryId;
    private String requestId;
    private String accountId;
    private String operation;
    private BigDecimal amount;
    private BigDecimal totalAfter;
    private BigDecimal availableAfter;
    private BigDecimal frozenAfter;
    private LocalDateTime createdAt;

    public AccountLedgerEntry() {
    }

    public AccountLedgerEntry(String entryId, String requestId, String accountId, String operation, BigDecimal amount,
                              BigDecimal totalAfter, BigDecimal availableAfter, BigDecimal frozenAfter) {
        this.entryId = entryId;
        this.requestId = requestId;
        this.accountId = accountId;
        this.operation = operation;
        this.amount = amount;
        this.totalAfter = totalAfter;
        this.availableAfter = availableAfter;
        this.frozenAfter = frozenAfter;
        this.createdAt = LocalDateTime.now();
    }

    public String getEntryId() {
        return entryId;
    }

    public void setEntryId(String entryId) {
        this.entryId = entryId;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getTotalAfter() {
        return totalAfter;
    }

    public void setTotalAfter(BigDecimal totalAfter) {
        this.totalAfter = totalAfter;
    }

    public BigDecimal getAvailableAfter() {
        return availableAfter;
    }

    public void setAvailableAfter(BigDecimal availableAfter) {
        this.availableAfter = availableAfter;
    }

    public BigDecimal getFrozenAfter() {
        return frozenAfter;
    }

    public void setFrozenAfter(BigDecimal frozenAfter) {
        this.frozenAfter = frozenAfter;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
