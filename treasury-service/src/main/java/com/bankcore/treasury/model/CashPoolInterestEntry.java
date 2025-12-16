package com.bankcore.treasury.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CashPoolInterestEntry {
    /** 计息记录主键 */
    private Long id;
    /** 关联现金池ID */
    private String poolId;
    /** 头寸账户 */
    private String headerAccount;
    /** 计息金额 */
    private BigDecimal interestAmount;
    /** 计息利率 */
    private BigDecimal rate;
    /** 计息日期 */
    private LocalDate accrualDate;
    /** 描述信息 */
    private String description;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public BigDecimal getInterestAmount() {
        return interestAmount;
    }

    public void setInterestAmount(BigDecimal interestAmount) {
        this.interestAmount = interestAmount;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    public LocalDate getAccrualDate() {
        return accrualDate;
    }

    public void setAccrualDate(LocalDate accrualDate) {
        this.accrualDate = accrualDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
