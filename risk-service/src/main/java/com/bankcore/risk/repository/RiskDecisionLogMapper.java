package com.bankcore.risk.repository;

import com.bankcore.risk.model.RiskDecisionLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

@Mapper
public interface RiskDecisionLogMapper {
    void insert(RiskDecisionLog log);

    BigDecimal sumForCustomerToday(@Param("customerId") String customerId);

    List<RiskDecisionLog> findRecent(@Param("limit") int limit);
}
