package com.bankcore.treasury.repository;

import com.bankcore.treasury.model.CashPoolInterestEntry;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public class CashPoolInterestRepository {
    private final CashPoolInterestMapper mapper;

    public CashPoolInterestRepository(CashPoolInterestMapper mapper) {
        this.mapper = mapper;
    }

    public void save(CashPoolInterestEntry entry) {
        mapper.insert(entry);
    }

    public List<CashPoolInterestEntry> findByPoolAndDate(String poolId, LocalDate accrualDate) {
        return mapper.findByPoolAndDate(poolId, accrualDate);
    }
}
