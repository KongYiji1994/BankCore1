package com.bankcore.treasury.repository;

import com.bankcore.treasury.model.CashPool;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CashPoolMapper {
    void insert(CashPool pool);

    CashPool findById(@Param("poolId") String poolId);

    List<CashPool> findAll();

    void updateTargetBalance(@Param("poolId") String poolId, @Param("targetBalance") java.math.BigDecimal targetBalance);
}
