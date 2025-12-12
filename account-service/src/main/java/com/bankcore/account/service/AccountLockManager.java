package com.bankcore.account.service;

import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 账户级分布式锁管理器：基于 Redis 保证同一账户在同一时间只被一个线程修改，避免并发超扣。
 */
@Component
public class AccountLockManager {
    private static final Logger log = LoggerFactory.getLogger(AccountLockManager.class);
    private static final String ACCOUNT_LOCK_KEY = "account:lock:";
    private final StringRedisTemplate redisTemplate;

    public AccountLockManager(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 获取账户写锁，返回 true 表示当前线程获得锁，可安全修改账户余额。
     */
    public boolean tryLock(String accountId, long ttlSeconds) {
        Boolean locked = redisTemplate.opsForValue()
                .setIfAbsent(ACCOUNT_LOCK_KEY + accountId, "1", ttlSeconds, TimeUnit.SECONDS);
        return locked != null && locked.booleanValue();
    }

    /**
     * 释放账户锁，异常时也尽量释放，避免长时间阻塞后续请求。
     */
    public void unlock(String accountId) {
        try {
            redisTemplate.delete(ACCOUNT_LOCK_KEY + accountId);
        } catch (Exception ex) {
            log.warn("释放账户锁失败 accountId={}", accountId, ex);
        }
    }
}
