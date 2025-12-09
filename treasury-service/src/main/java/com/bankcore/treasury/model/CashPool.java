package com.bankcore.treasury.model;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

public class CashPool {
    private final String poolId;
    private final String headerAccount;
    private final Set<String> memberAccounts = new HashSet<>();
    private BigDecimal targetBalance;
    private final String strategy;

    public CashPool(String poolId, String headerAccount, Set<String> members, BigDecimal targetBalance, String strategy) {
        this.poolId = poolId;
        this.headerAccount = headerAccount;
        this.memberAccounts.addAll(members);
        this.targetBalance = targetBalance;
        this.strategy = strategy;
    }

    public String getPoolId() {
        return poolId;
    }

    public String getHeaderAccount() {
        return headerAccount;
    }

    public Set<String> getMemberAccounts() {
        return memberAccounts;
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
}
