package com.bankcore.settlement.model;

import java.math.BigDecimal;

public class ReconciliationBreak {
    /** 差异记录主键 */
    private Long id;
    /** 对应汇总记录ID */
    private Long summaryId;
    /** 内部支付指令号 */
    private String instructionId;
    /** 外部参考号 */
    private String externalReference;
    /** 差异类型 */
    private ReconciliationBreakType breakType;
    /** 内部记录金额 */
    private BigDecimal internalAmount;
    /** 外部记录金额 */
    private BigDecimal externalAmount;
    /** 交易币种 */
    private String currency;
    /** 备注说明 */
    private String remark;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSummaryId() {
        return summaryId;
    }

    public void setSummaryId(Long summaryId) {
        this.summaryId = summaryId;
    }

    public String getInstructionId() {
        return instructionId;
    }

    public void setInstructionId(String instructionId) {
        this.instructionId = instructionId;
    }

    public String getExternalReference() {
        return externalReference;
    }

    public void setExternalReference(String externalReference) {
        this.externalReference = externalReference;
    }

    public ReconciliationBreakType getBreakType() {
        return breakType;
    }

    public void setBreakType(ReconciliationBreakType breakType) {
        this.breakType = breakType;
    }

    public BigDecimal getInternalAmount() {
        return internalAmount;
    }

    public void setInternalAmount(BigDecimal internalAmount) {
        this.internalAmount = internalAmount;
    }

    public BigDecimal getExternalAmount() {
        return externalAmount;
    }

    public void setExternalAmount(BigDecimal externalAmount) {
        this.externalAmount = externalAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
