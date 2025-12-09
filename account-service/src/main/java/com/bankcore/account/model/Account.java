package com.bankcore.account.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Account {
    private String accountId;
    private String customerId;
    private String currency;
    private BigDecimal balance;
    private BigDecimal availableBalance;
    private LocalDateTime openedAt;

    public Account() {
    }

    public Account(String accountId, String customerId, String currency, BigDecimal openingBalance) {
        this.accountId = accountId;
        this.customerId = customerId;
        this.currency = currency;
        this.balance = openingBalance;
        this.availableBalance = openingBalance;
        this.openedAt = LocalDateTime.now();
    }

    public synchronized void credit(BigDecimal amount) {
        balance = balance.add(amount);
        availableBalance = availableBalance.add(amount);
    }

    public synchronized void debit(BigDecimal amount) {
        if (availableBalance.compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient available balance");
        }
        balance = balance.subtract(amount);
        availableBalance = availableBalance.subtract(amount);
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public BigDecimal getAvailableBalance() {
        return availableBalance;
    }

    public void setAvailableBalance(BigDecimal availableBalance) {
        this.availableBalance = availableBalance;
    }

    public LocalDateTime getOpenedAt() {
        return openedAt;
    }

    public void setOpenedAt(LocalDateTime openedAt) {
        this.openedAt = openedAt;
    }
}
