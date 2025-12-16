package com.bankcore.account.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Account {
    /** 账户唯一标识 */
    private String accountId;
    /** 关联客户ID */
    private String customerId;
    /** 账户币种 */
    private String currency;
    /** 当前可用余额 */
    private BigDecimal availableBalance;
    /** 当前总余额（含冻结部分） */
    private BigDecimal totalBalance;
    /** 已冻结余额 */
    private BigDecimal frozenBalance;
    /** 账户状态 */
    private String status;
    /** 开户时间 */
    private LocalDateTime openedAt;

    public Account() {
    }

    public Account(String accountId, String customerId, String currency, BigDecimal openingBalance) {
        this.accountId = accountId;
        this.customerId = customerId;
        this.currency = currency;
        this.totalBalance = openingBalance;
        this.availableBalance = openingBalance;
        this.frozenBalance = BigDecimal.ZERO;
        this.status = "ACTIVE";
        this.openedAt = LocalDateTime.now();
    }

    public synchronized void credit(BigDecimal amount) {
        ensureOperable();
        totalBalance = totalBalance.add(amount);
        availableBalance = availableBalance.add(amount);
    }

    public synchronized void prepareDebit(BigDecimal amount) {
        ensureDebitAllowed();
        if (availableBalance.compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient available balance to freeze");
        }
        availableBalance = availableBalance.subtract(amount);
        frozenBalance = frozenBalance.add(amount);
    }

    public synchronized void settleDebit(BigDecimal amount) {
        ensureDebitAllowed();
        if (frozenBalance.compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient frozen balance to settle");
        }
        totalBalance = totalBalance.subtract(amount);
        frozenBalance = frozenBalance.subtract(amount);
    }

    public synchronized void releaseFrozen(BigDecimal amount) {
        ensureOperable();
        if (frozenBalance.compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient frozen balance to release");
        }
        frozenBalance = frozenBalance.subtract(amount);
        availableBalance = availableBalance.add(amount);
    }

    public synchronized void close() {
        if (totalBalance.compareTo(BigDecimal.ZERO) != 0 || availableBalance.compareTo(BigDecimal.ZERO) != 0 || frozenBalance
                .compareTo(BigDecimal.ZERO) != 0) {
            throw new IllegalStateException("Account must be fully settled before closing");
        }
        status = "CLOSED";
    }

    private void ensureOperable() {
        if ("CLOSED".equalsIgnoreCase(status)) {
            throw new IllegalStateException("Account is closed");
        }
    }

    private void ensureDebitAllowed() {
        ensureOperable();
        if ("FROZEN".equalsIgnoreCase(status)) {
            throw new IllegalStateException("Account is frozen for debit operations");
        }
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

    public BigDecimal getAvailableBalance() {
        return availableBalance;
    }

    public void setAvailableBalance(BigDecimal availableBalance) {
        this.availableBalance = availableBalance;
    }

    public BigDecimal getTotalBalance() {
        return totalBalance;
    }

    public void setTotalBalance(BigDecimal totalBalance) {
        this.totalBalance = totalBalance;
    }

    public BigDecimal getFrozenBalance() {
        return frozenBalance;
    }

    public void setFrozenBalance(BigDecimal frozenBalance) {
        this.frozenBalance = frozenBalance;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getOpenedAt() {
        return openedAt;
    }

    public void setOpenedAt(LocalDateTime openedAt) {
        this.openedAt = openedAt;
    }
}
