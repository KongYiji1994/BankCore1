package com.bankcore.account.model;

import java.math.BigDecimal;
import java.time.Instant;

public class Account {
    private final String accountId;
    private final String customerId;
    private final String currency;
    private BigDecimal balance;
    private BigDecimal availableBalance;
    private final Instant openedAt;

    public Account(String accountId, String customerId, String currency, BigDecimal openingBalance) {
        this.accountId = accountId;
        this.customerId = customerId;
        this.currency = currency;
        this.balance = openingBalance;
        this.availableBalance = openingBalance;
        this.openedAt = Instant.now();
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

    public String getCustomerId() {
        return customerId;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public BigDecimal getAvailableBalance() {
        return availableBalance;
    }

    public Instant getOpenedAt() {
        return openedAt;
    }
}
