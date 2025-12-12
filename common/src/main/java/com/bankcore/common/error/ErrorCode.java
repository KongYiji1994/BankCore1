package com.bankcore.common.error;

public enum ErrorCode {
    INVALID_REQUEST("INVALID_REQUEST"),
    NOT_FOUND("NOT_FOUND"),
    BUSINESS_RULE_VIOLATION("BUSINESS_RULE_VIOLATION"),
    RISK_REJECTED("RISK_REJECTED"),
    PROCESSING("PROCESSING"),
    INTERNAL_ERROR("INTERNAL_ERROR");

    private final String code;

    ErrorCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
