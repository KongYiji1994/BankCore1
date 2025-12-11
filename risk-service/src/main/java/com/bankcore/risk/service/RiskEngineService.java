package com.bankcore.risk.service;

import com.bankcore.risk.model.RiskDecision;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
public class RiskEngineService {
    public RiskDecision evaluate(BigDecimal amount, String customerId, String channel) {
        RiskDecision decision = new RiskDecision();
        decision.setDecisionId(UUID.randomUUID().toString());
        boolean blocked = amount.compareTo(new BigDecimal("500000")) > 0;
        decision.setBlocked(blocked);
        decision.setLevel(blocked ? "HIGH" : "LOW");
        decision.setReason(blocked ? "Amount exceeds per-transaction limit" : "PASS at " + Instant.now());
        return decision;
    }
}
