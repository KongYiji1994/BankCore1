package com.bankcore.treasury.repository;

import com.bankcore.treasury.model.CashPool;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CashPoolRepository {
    private final CashPoolMapper mapper;

    public CashPoolRepository(CashPoolMapper mapper) {
        this.mapper = mapper;
    }

    public void save(CashPool pool) {
        mapper.insert(pool);
    }

    public Optional<CashPool> findById(String poolId) {
        return Optional.ofNullable(mapper.findById(poolId));
    }

    public List<CashPool> findAll() {
        return mapper.findAll();
    }

    public void updateTargetBalance(String poolId, java.math.BigDecimal balance) {
        mapper.updateTargetBalance(poolId, balance);
    }

    public void updateInterestDate(String poolId, java.time.LocalDate lastInterestDate) {
        mapper.updateInterestDate(poolId, lastInterestDate);
    }
}
