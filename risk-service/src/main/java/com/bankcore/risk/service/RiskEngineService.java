package com.bankcore.risk.service;

import com.bankcore.risk.api.RiskController;
import com.bankcore.risk.model.RiskDecision;
import com.bankcore.risk.model.RiskDecisionLog;
import com.bankcore.risk.model.RiskDecisionResult;
import com.bankcore.risk.model.RiskRule;
import com.bankcore.risk.repository.RiskDecisionLogRepository;
import com.bankcore.risk.repository.RiskRuleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class RiskEngineService {
    private static final String RULE_LIMIT_PER_TXN = "LIMIT_PER_TXN";
    private static final String RULE_LIMIT_DAILY = "LIMIT_DAILY";
    private static final String RULE_BLACKLIST = "BLACKLIST";
    private static final Logger log = LoggerFactory.getLogger(RiskEngineService.class);

    private final RiskRuleRepository riskRuleRepository;
    private final RiskDecisionLogRepository decisionLogRepository;

    public RiskEngineService(RiskRuleRepository riskRuleRepository,
                             RiskDecisionLogRepository decisionLogRepository) {
        this.riskRuleRepository = riskRuleRepository;
        this.decisionLogRepository = decisionLogRepository;
    }

    public RiskDecision evaluate(RiskController.RiskRequest request) {
        List<RiskRule> rules = riskRuleRepository.findEnabled();
        log.info("evaluating risk for customer={}, account={}, amount={}",
                request.getCustomerId(), request.getPayerAccount(), request.getAmount());
        RiskDecision decision = new RiskDecision();
        decision.setDecisionId(UUID.randomUUID().toString());
        decision.setResult(RiskDecisionResult.APPROVED);
        decision.setReason("Passed at " + Instant.now());
        decision.setLevel("LOW");

        if (rules != null) {
            for (RiskRule rule : rules) {
                String type = rule.getType() == null ? "" : rule.getType().toUpperCase(Locale.ENGLISH);
                if (RULE_BLACKLIST.equals(type) && isBlacklisted(rule, request)) {
                    populateDecision(decision, rule, RiskDecisionResult.REJECTED,
                            "Customer or account on blacklist");
                    break;
                }
                if (RULE_LIMIT_PER_TXN.equals(type)
                        && request.getAmount() != null
                        && rule.getThreshold() != null
                        && request.getAmount().compareTo(rule.getThreshold()) > 0) {
                    populateDecision(decision, rule, RiskDecisionResult.REVIEW,
                            "Amount exceeds per-transaction threshold " + rule.getThreshold());
                    break;
                }
                if (RULE_LIMIT_DAILY.equals(type)
                        && rule.getThreshold() != null
                        && violatesDailyLimit(request.getCustomerId(), request.getAmount(), rule.getThreshold())) {
                    populateDecision(decision, rule, RiskDecisionResult.REJECTED,
                            "Daily cumulative limit exceeded for customer " + request.getCustomerId());
                    break;
                }
            }
        }

        saveAudit(decision, request);
        return decision;
    }

    private boolean isBlacklisted(RiskRule rule, RiskController.RiskRequest request) {
        if (rule.getExpression() == null) {
            return false;
        }
        String[] tokens = rule.getExpression().split(",");
        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i].trim();
            if (token.length() == 0) {
                continue;
            }
            if (token.equalsIgnoreCase(request.getCustomerId()) || token.equalsIgnoreCase(request.getPayerAccount())) {
                return true;
            }
        }
        return false;
    }

    private boolean violatesDailyLimit(String customerId, BigDecimal amount, BigDecimal threshold) {
        if (customerId == null || amount == null || threshold == null) {
            return false;
        }
        BigDecimal usedToday = decisionLogRepository.sumToday(customerId);
        return usedToday.add(amount).compareTo(threshold) > 0;
    }

    private void populateDecision(RiskDecision decision, RiskRule rule, RiskDecisionResult result, String reason) {
        decision.setResult(result);
        decision.setReason(reason);
        decision.setRuleId(rule.getId());
        decision.setRuleType(rule.getType());
        decision.setLevel(result == RiskDecisionResult.REJECTED ? "HIGH" : "MEDIUM");
        decision.setBlocked(result == RiskDecisionResult.REJECTED);
    }

    private void saveAudit(RiskDecision decision, RiskController.RiskRequest request) {
        RiskDecisionLog log = new RiskDecisionLog();
        log.setCustomerId(request.getCustomerId());
        log.setPayerAccount(request.getPayerAccount());
        log.setAmount(request.getAmount());
        log.setResult(decision.getResult().name());
        log.setRuleId(decision.getRuleId());
        log.setRuleType(decision.getRuleType());
        decisionLogRepository.save(log);
    }
}
