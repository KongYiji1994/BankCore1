package com.bankcore.payment.service;

import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 支付幂等组件：利用 Redis 分布式锁与标记，保护请求提交与事件消费的唯一性。
 */
@Component
public class PaymentIdempotencyManager {
    private static final Logger log = LoggerFactory.getLogger(PaymentIdempotencyManager.class);
    private static final String REQUEST_LOCK_KEY = "payment:req:lock:";
    private static final String EVENT_PROCESSING_KEY = "payment:event:processing:";
    private static final String EVENT_DONE_KEY = "payment:event:done:";
    private final StringRedisTemplate redisTemplate;

    /**
     * 构造注入 Redis 客户端。
     */
    public PaymentIdempotencyManager(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 尝试获取请求级锁，防止同一 requestId 重复提交。
     */
    public boolean tryAcquireRequestLock(String requestId, long ttlSeconds) {
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(REQUEST_LOCK_KEY + requestId, "1", ttlSeconds, TimeUnit.SECONDS);
        return locked != null && locked.booleanValue();
    }

    /**
     * 释放请求锁，提交完成后调用。
     */
    public void releaseRequestLock(String requestId) {
        try {
            redisTemplate.delete(REQUEST_LOCK_KEY + requestId);
        } catch (Exception ex) {
            log.warn("Failed to release request lock for {}", requestId, ex);
        }
    }

    /**
     * 判断消费是否已经完成，防止 MQ 重复投递导致二次扣款。
     */
    public boolean isEventAlreadyProcessed(String instructionId) {
        String key = EVENT_DONE_KEY + instructionId;
        Boolean exists = redisTemplate.hasKey(key);
        return exists != null && exists.booleanValue();
    }

    /**
     * 尝试获取消费中的标记，同一笔指令只允许一个消费者处理。
     */
    public boolean tryAcquireEventProcessing(String instructionId, long ttlSeconds) {
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(EVENT_PROCESSING_KEY + instructionId, "1", ttlSeconds, TimeUnit.SECONDS);
        return locked != null && locked.booleanValue();
    }

    /**
     * 标记事件处理完成，设置过期时间用于幂等窗口。
     */
    public void markEventCompleted(String instructionId, long ttlSeconds) {
        redisTemplate.opsForValue().set(EVENT_DONE_KEY + instructionId, "1", ttlSeconds, TimeUnit.SECONDS);
        redisTemplate.delete(EVENT_PROCESSING_KEY + instructionId);
    }

    /**
     * 释放事件处理锁，异常时兜底释放。
     */
    public void releaseEventLock(String instructionId) {
        redisTemplate.delete(EVENT_PROCESSING_KEY + instructionId);
    }
}
