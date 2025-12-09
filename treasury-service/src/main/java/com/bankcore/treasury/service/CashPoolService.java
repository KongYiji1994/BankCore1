package com.bankcore.treasury.service;

import com.bankcore.common.dto.CashPoolDefinition;
import com.bankcore.treasury.model.CashPool;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class CashPoolService {
    private final CashPoolRepository repository = new CashPoolRepository();

    public CashPool register(CashPoolDefinition definition) {
        CashPool pool = new CashPool(definition.poolId(), definition.headerAccount(), definition.memberAccounts(), definition.targetBalance(), definition.strategy());
        repository.save(pool);
        return pool;
    }

    public CashPool sweep(String poolId, BigDecimal headerBalance) {
        CashPool pool = repository.findById(poolId)
                .orElseThrow(() -> new IllegalArgumentException("Pool not found"));
        // In a real system we would fetch member balances and post transfers; here we simulate by adjusting targetBalance.
        pool.setTargetBalance(headerBalance.add(pool.getTargetBalance()));
        return pool;
    }

    public CashPool get(String poolId) {
        return repository.findById(poolId)
                .orElseThrow(() -> new IllegalArgumentException("Pool not found"));
    }

    public List<CashPool> list() {
        return repository.findAll().values().stream().toList();
    }
}
