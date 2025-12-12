package com.bankcore.payment.service;

import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 支付侧账户锁：用于跨服务控制同一账户的并发出款，防止重复冻结或超扣。
 */
@Component
public class PaymentAccountLockManager {
    private static final Logger log = LoggerFactory.getLogger(PaymentAccountLockManager.class);
    private static final String PAYMENT_ACCOUNT_LOCK_KEY = "payment:acct:lock:";
    private final StringRedisTemplate redisTemplate;

    public PaymentAccountLockManager(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 尝试加锁，返回 true 表示本次消费/请求可以操作该账户。
     */
    public boolean tryLock(String accountId, long ttlSeconds) {
        Boolean locked = redisTemplate.opsForValue()
                .setIfAbsent(PAYMENT_ACCOUNT_LOCK_KEY + accountId, "1", ttlSeconds, TimeUnit.SECONDS);
        return locked != null && locked.booleanValue();
    }

    /**
     * 释放锁，失败时写入告警日志避免长时间占用。
     */
    public void unlock(String accountId) {
        try {
            redisTemplate.delete(PAYMENT_ACCOUNT_LOCK_KEY + accountId);
        } catch (Exception ex) {
            log.warn("释放支付账户锁失败 accountId={}", accountId, ex);
        }
    }
}
