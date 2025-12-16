package com.bankcore.treasury.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class CashPool {
    /** 现金池ID */
    private String poolId;
    /** 头寸账户 */
    private String headerAccount;
    /** 成员账户集合 */
    private final Set<String> memberAccounts = new HashSet<>();
    /** 目标余额或留存额度 */
    private BigDecimal targetBalance;
    /** 调拨策略 */
    private String strategy;
    /** 现金池类型 */
    private String poolType;
    /** 利率或分摊比例 */
    private BigDecimal interestRate;
    /** 上一次计息日期 */
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
