package com.bankcore.settlement.model;

import java.math.BigDecimal;

public class ExternalPaymentRecord {
    /** 外部渠道指令号 */
    private String instructionId;
    /** 外部参考号或流水号 */
    private String externalReference;
    /** 外部记录金额 */
    private BigDecimal amount;
    /** 交易币种 */
    private String currency;
    /** 付款方账户 */
    private String payerAccount;
    /** 收款方账户 */
    private String payeeAccount;

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

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getPayerAccount() {
        return payerAccount;
    }

    public void setPayerAccount(String payerAccount) {
        this.payerAccount = payerAccount;
    }

    public String getPayeeAccount() {
        return payeeAccount;
    }

    public void setPayeeAccount(String payeeAccount) {
        this.payeeAccount = payeeAccount;
    }
}
