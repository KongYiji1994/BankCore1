package com.bankcore.common.dto;

public enum PaymentStatus {
    PENDING,
    INITIATED,
    IN_RISK_REVIEW,
    RISK_REJECTED,
    RISK_APPROVED,
    CLEARING,
    POSTED,
    FAILED
}
