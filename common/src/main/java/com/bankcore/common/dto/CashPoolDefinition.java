package com.bankcore.common.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Set;

public class CashPoolDefinition {
    @NotBlank
    private String poolId;
    @NotBlank
    private String headerAccount;
    @NotNull
    private Set<String> memberAccounts;
    @NotNull
    private BigDecimal targetBalance;
    @NotBlank
    private String strategy;
    @NotBlank
    private String poolType;
    @NotNull
    private BigDecimal interestRate;

    public CashPoolDefinition() {
    }

    public CashPoolDefinition(String poolId, String headerAccount, Set<String> memberAccounts, BigDecimal targetBalance, String strategy,
                              String poolType, BigDecimal interestRate) {
        this.poolId = poolId;
        this.headerAccount = headerAccount;
        this.memberAccounts = memberAccounts;
        this.targetBalance = targetBalance;
        this.strategy = strategy;
        this.poolType = poolType;
        this.interestRate = interestRate;
    }

    public String getPoolId() {
        return poolId;
    }

    public void setPoolId(String poolId) {
        this.poolId = poolId;
    }

    public String getHeaderAccount() {
        return headerAccount;
    }

    public void setHeaderAccount(String headerAccount) {
        this.headerAccount = headerAccount;
    }

    public Set<String> getMemberAccounts() {
        return memberAccounts;
    }

    public void setMemberAccounts(Set<String> memberAccounts) {
        this.memberAccounts = memberAccounts;
    }

    public BigDecimal getTargetBalance() {
        return targetBalance;
    }

    public void setTargetBalance(BigDecimal targetBalance) {
        this.targetBalance = targetBalance;
    }

    public String getStrategy() {
        return strategy;
    }

    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }

    public String getPoolType() {
        return poolType;
    }

    public void setPoolType(String poolType) {
        this.poolType = poolType;
    }

    public BigDecimal getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(BigDecimal interestRate) {
        this.interestRate = interestRate;
    }
}
