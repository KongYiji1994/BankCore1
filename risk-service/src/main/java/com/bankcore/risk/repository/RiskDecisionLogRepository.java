package com.bankcore.risk.repository;

import com.bankcore.risk.model.RiskDecisionLog;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public class RiskDecisionLogRepository {
    private final RiskDecisionLogMapper mapper;

    public RiskDecisionLogRepository(RiskDecisionLogMapper mapper) {
        this.mapper = mapper;
    }

    public void save(RiskDecisionLog log) {
        mapper.insert(log);
    }

    public BigDecimal sumToday(String customerId) {
        BigDecimal total = mapper.sumForCustomerToday(customerId);
        return total == null ? BigDecimal.ZERO : total;
    }

    public List<RiskDecisionLog> latest(int limit) {
        return mapper.findRecent(limit);
    }
}
