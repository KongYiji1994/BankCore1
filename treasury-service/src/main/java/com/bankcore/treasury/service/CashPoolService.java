package com.bankcore.treasury.service;

import com.bankcore.common.dto.CashPoolDefinition;
import com.bankcore.treasury.model.CashPool;
import com.bankcore.treasury.repository.CashPoolRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CashPoolService {
    private final CashPoolRepository repository;

    public CashPoolService(CashPoolRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public CashPool register(CashPoolDefinition definition) {
        CashPool pool = new CashPool(definition.getPoolId(), definition.getHeaderAccount(), definition.getMemberAccounts(), definition.getTargetBalance(), definition.getStrategy());
        repository.save(pool);
        return pool;
    }

    @Transactional
    public CashPool sweep(String poolId, BigDecimal headerBalance) {
        CashPool pool = repository.findById(poolId)
                .orElseThrow(() -> new IllegalArgumentException("Pool not found"));
        pool.setTargetBalance(headerBalance.add(pool.getTargetBalance()));
        repository.updateTargetBalance(poolId, pool.getTargetBalance());
        return pool;
    }

    public CashPool get(String poolId) {
        return repository.findById(poolId)
                .orElseThrow(() -> new IllegalArgumentException("Pool not found"));
    }

    public List<CashPool> list() {
        return repository.findAll();
    }
}
