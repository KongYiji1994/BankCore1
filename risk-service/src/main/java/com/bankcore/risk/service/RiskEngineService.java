package com.bankcore.risk.service;

import com.bankcore.risk.api.RiskController;
import com.bankcore.risk.model.RiskDecision;
import com.bankcore.risk.model.RiskDecisionLog;
import com.bankcore.risk.model.RiskDecisionResult;
import com.bankcore.risk.model.RiskRule;
import com.bankcore.risk.repository.RiskDecisionLogRepository;
import com.bankcore.risk.repository.RiskRuleRepository;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * 风控引擎：加载启用的规则，依次校验黑名单、单笔限额、日累计限额，并记录审计日志。
 */
@Service
public class RiskEngineService {
    private static final String RULE_LIMIT_PER_TXN = "LIMIT_PER_TXN";
    private static final String RULE_LIMIT_DAILY = "LIMIT_DAILY";
    private static final String RULE_BLACKLIST = "BLACKLIST";
    private static final String RULE_HIGH_FREQ = "HIGH_FREQ";
    private static final Logger log = LoggerFactory.getLogger(RiskEngineService.class);

    private final RiskRuleRepository riskRuleRepository;
    private final RiskDecisionLogRepository decisionLogRepository;
    private final RiskCacheManager riskCacheManager;

    /**
     * 构造注入规则仓储与决策日志仓储。
     */
    public RiskEngineService(RiskRuleRepository riskRuleRepository,
                             RiskDecisionLogRepository decisionLogRepository,
                             RiskCacheManager riskCacheManager) {
        this.riskRuleRepository = riskRuleRepository;
        this.decisionLogRepository = decisionLogRepository;
        this.riskCacheManager = riskCacheManager;
    }

    /**
     * 执行风控评估：遍历规则并生成决策，默认通过。
     */
    public RiskDecision evaluate(RiskController.RiskRequest request) {
        RiskDecision cached = riskCacheManager.getCachedDecision(request.getRequestId());
        if (cached != null) {
            log.info("reuse cached risk decision for requestId={}", request.getRequestId());
            return cached;
        }
        if (!riskCacheManager.tryMarkProcessing(request.getRequestId())) {
            RiskDecision pending = new RiskDecision();
            pending.setDecisionId(UUID.randomUUID().toString());
            pending.setResult(RiskDecisionResult.REVIEW);
            pending.setReason("Duplicate risk evaluation in progress");
            return pending;
        }
        // 通过 Redis 做规则缓存，减少每次支付都访问数据库的开销，5 分钟刷新一次
        List<RiskRule> rules = riskCacheManager.loadEnabledRules(new Supplier<List<RiskRule>>() {
            @Override
            public List<RiskRule> get() {
                return riskRuleRepository.findEnabled();
            }
        });
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
                if (RULE_HIGH_FREQ.equals(type)
                        && rule.getThreshold() != null
                        && violatesFrequency(request.getCustomerId(), rule.getThreshold().intValue())) {
                    populateDecision(decision, rule, RiskDecisionResult.REJECTED,
                            "High frequency transactions detected for customer " + request.getCustomerId());
                    break;
                }
            }
        }

        saveAudit(decision, request);
        riskCacheManager.cacheDecision(request.getRequestId(), decision);
        return decision;
    }

    /**
     * 校验客户或账户是否命中黑名单表达式。
     */
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

    /**
     * 计算当日累加金额是否超限。
     */
    private boolean violatesDailyLimit(String customerId, BigDecimal amount, BigDecimal threshold) {
        if (customerId == null || amount == null || threshold == null) {
            return false;
        }
        // 优先使用 Redis 计数器累加日额度，避免高并发下聚合查询数据库
        Long cachedTotal = riskCacheManager.incrementDailyAmount(customerId, amount);
        if (cachedTotal != null) {
            BigDecimal cachedAmount = new BigDecimal(cachedTotal).movePointLeft(2);
            return cachedAmount.compareTo(threshold) > 0;
        }
        BigDecimal usedToday = decisionLogRepository.sumToday(customerId);
        return usedToday.add(amount).compareTo(threshold) > 0;
    }

    /**
     * 高频检测：使用 Redis 分钟计数，若计数异常则默认放行并依赖后续规则兜底。
     */
    private boolean violatesFrequency(String customerId, int threshold) {
        // 高频规则：Redis 按分钟累加，超过阈值直接拒绝，计数异常时返回 false 由后续规则兜底
        Long count = riskCacheManager.incrementFrequency(customerId);
        if (count != null) {
            return count.intValue() > threshold;
        }
        return false;
    }

    /**
     * 填充决策对象的结果与原因，用于外部返回与日志。
     */
    private void populateDecision(RiskDecision decision, RiskRule rule, RiskDecisionResult result, String reason) {
        decision.setResult(result);
        decision.setReason(reason);
        decision.setRuleId(rule.getId());
        decision.setRuleType(rule.getType());
        decision.setLevel(result == RiskDecisionResult.REJECTED ? "HIGH" : "MEDIUM");
        decision.setBlocked(result == RiskDecisionResult.REJECTED);
    }

    /**
     * 记录风控审计日志，支持日累计查询。
     */
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
