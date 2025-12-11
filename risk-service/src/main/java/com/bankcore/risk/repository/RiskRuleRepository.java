package com.bankcore.risk.repository;

import com.bankcore.risk.model.RiskRule;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class RiskRuleRepository {
    private final RiskRuleMapper mapper;

    public RiskRuleRepository(RiskRuleMapper mapper) {
        this.mapper = mapper;
    }

    public List<RiskRule> findEnabled() {
        return mapper.findEnabled();
    }
}
