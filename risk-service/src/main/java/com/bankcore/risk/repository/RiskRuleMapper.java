package com.bankcore.risk.repository;

import com.bankcore.risk.model.RiskRule;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface RiskRuleMapper {
    List<RiskRule> findEnabled();
}
