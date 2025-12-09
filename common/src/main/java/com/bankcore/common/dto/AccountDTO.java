package com.bankcore.common.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

public class AccountDTO {
    @NotBlank
    private String accountId;
    @NotBlank
    private String customerId;
    @NotBlank
    private String currency;
    @NotNull
    @Min(0)
    private BigDecimal balance;
    @NotNull
    @Min(0)
    private BigDecimal availableBalance;

    public AccountDTO() {
    }

    public AccountDTO(String accountId, String customerId, String currency, BigDecimal balance, BigDecimal availableBalance) {
        this.accountId = accountId;
        this.customerId = customerId;
        this.currency = currency;
        this.balance = balance;
        this.availableBalance = availableBalance;
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
}
