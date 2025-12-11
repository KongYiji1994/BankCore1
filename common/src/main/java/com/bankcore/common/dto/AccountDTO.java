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
    private BigDecimal totalBalance;
    @NotNull
    @Min(0)
    private BigDecimal availableBalance;
    @NotNull
    @Min(0)
    private BigDecimal frozenBalance;
    @NotBlank
    private String status;

    public AccountDTO() {
    }

    public AccountDTO(String accountId, String customerId, String currency, BigDecimal totalBalance, BigDecimal availableBalance,
                      BigDecimal frozenBalance, String status) {
        this.accountId = accountId;
        this.customerId = customerId;
        this.currency = currency;
        this.totalBalance = totalBalance;
        this.availableBalance = availableBalance;
        this.frozenBalance = frozenBalance;
        this.status = status;
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

    public BigDecimal getTotalBalance() {
        return totalBalance;
    }

    public void setTotalBalance(BigDecimal totalBalance) {
        this.totalBalance = totalBalance;
    }

    public BigDecimal getAvailableBalance() {
        return availableBalance;
    }

    public void setAvailableBalance(BigDecimal availableBalance) {
        this.availableBalance = availableBalance;
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
}
