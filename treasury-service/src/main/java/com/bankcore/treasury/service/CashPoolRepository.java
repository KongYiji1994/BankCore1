package com.bankcore.treasury.service;

import com.bankcore.treasury.model.CashPool;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class CashPoolRepository {
    private final Map<String, CashPool> pools = new ConcurrentHashMap<>();

    public void save(CashPool pool) {
        pools.put(pool.getPoolId(), pool);
    }

    public Optional<CashPool> findById(String poolId) {
        return Optional.ofNullable(pools.get(poolId));
    }

    public Map<String, CashPool> findAll() {
        return pools;
    }
}
