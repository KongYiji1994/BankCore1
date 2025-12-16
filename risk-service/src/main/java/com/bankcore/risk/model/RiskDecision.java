package com.bankcore.risk.model;

public class RiskDecision {
    /** 决策流水号 */
    private String decisionId;
    /** 决策结果 */
    private RiskDecisionResult result;
    /** 决策原因描述 */
    private String reason;
    /** 命中的规则ID */
    private Long ruleId;
    /** 规则类型 */
    private String ruleType;
    /** 风险等级 */
    private String level;
    /** 是否被拦截 */
    private boolean blocked;

    public String getDecisionId() {
        return decisionId;
    }

    public void setDecisionId(String decisionId) {
        this.decisionId = decisionId;
    }

    public RiskDecisionResult getResult() {
        return result;
    }

    public void setResult(RiskDecisionResult result) {
        this.result = result;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Long getRuleId() {
        return ruleId;
    }

    public void setRuleId(Long ruleId) {
        this.ruleId = ruleId;
    }

    public String getRuleType() {
        return ruleType;
    }

    public void setRuleType(String ruleType) {
        this.ruleType = ruleType;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }
}
