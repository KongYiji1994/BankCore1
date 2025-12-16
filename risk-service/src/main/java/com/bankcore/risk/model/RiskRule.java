package com.bankcore.risk.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RiskRule {
    /** 规则ID */
    private Long id;
    /** 规则名称 */
    private String name;
    /** 规则类型 */
    private String type;
    /** 规则表达式或脚本 */
    private String expression;
    /** 阈值或触发条件 */
    private BigDecimal threshold;
    /** 是否启用 */
    private boolean enabled;
    /** 创建时间 */
    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public BigDecimal getThreshold() {
        return threshold;
    }

    public void setThreshold(BigDecimal threshold) {
        this.threshold = threshold;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
