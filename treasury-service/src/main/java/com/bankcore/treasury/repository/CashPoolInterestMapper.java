package com.bankcore.treasury.repository;

import com.bankcore.treasury.model.CashPoolInterestEntry;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface CashPoolInterestMapper {
    void insert(CashPoolInterestEntry entry);

    List<CashPoolInterestEntry> findByPoolAndDate(String poolId, LocalDate accrualDate);
}
