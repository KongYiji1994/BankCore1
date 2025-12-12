package com.bankcore.treasury.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class CashPool {
    private String poolId;
    private String headerAccount;
    private final Set<String> memberAccounts = new HashSet<>();
    private BigDecimal targetBalance;
    private String strategy;
    private String poolType;
    private BigDecimal interestRate;
    private LocalDate lastInterestDate;

    public CashPool() {
    }

    public CashPool(String poolId, String headerAccount, Set<String> members, BigDecimal targetBalance, String strategy,
                    String poolType, BigDecimal interestRate) {
        this.poolId = poolId;
        this.headerAccount = headerAccount;
        this.memberAccounts.addAll(members);
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

    public String getMemberAccountsCsv() {
        return String.join(",", memberAccounts);
    }

    public void setMemberAccountsCsv(String members) {
        this.memberAccounts.clear();
        if (members != null && !members.isEmpty()) {
            this.memberAccounts.addAll(Arrays.asList(members.split(",")));
        }
    }

    public void setMemberAccounts(Set<String> members) {
        this.memberAccounts.clear();
        if (members != null) {
            this.memberAccounts.addAll(members);
        }
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

    public LocalDate getLastInterestDate() {
        return lastInterestDate;
    }

    public void setLastInterestDate(LocalDate lastInterestDate) {
        this.lastInterestDate = lastInterestDate;
    }
}
