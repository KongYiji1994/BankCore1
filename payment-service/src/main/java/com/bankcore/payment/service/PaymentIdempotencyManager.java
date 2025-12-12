package com.bankcore.payment.service;

import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaymentIdempotencyManager {
    private static final Logger log = LoggerFactory.getLogger(PaymentIdempotencyManager.class);
    private static final String REQUEST_LOCK_KEY = "payment:req:lock:";
    private static final String EVENT_PROCESSING_KEY = "payment:event:processing:";
    private static final String EVENT_DONE_KEY = "payment:event:done:";
    private final StringRedisTemplate redisTemplate;

    public PaymentIdempotencyManager(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean tryAcquireRequestLock(String requestId, long ttlSeconds) {
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(REQUEST_LOCK_KEY + requestId, "1", ttlSeconds, TimeUnit.SECONDS);
        return locked != null && locked.booleanValue();
    }

    public void releaseRequestLock(String requestId) {
        try {
            redisTemplate.delete(REQUEST_LOCK_KEY + requestId);
        } catch (Exception ex) {
            log.warn("Failed to release request lock for {}", requestId, ex);
        }
    }

    public boolean isEventAlreadyProcessed(String instructionId) {
        String key = EVENT_DONE_KEY + instructionId;
        Boolean exists = redisTemplate.hasKey(key);
        return exists != null && exists.booleanValue();
    }

    public boolean tryAcquireEventProcessing(String instructionId, long ttlSeconds) {
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(EVENT_PROCESSING_KEY + instructionId, "1", ttlSeconds, TimeUnit.SECONDS);
        return locked != null && locked.booleanValue();
    }

    public void markEventCompleted(String instructionId, long ttlSeconds) {
        redisTemplate.opsForValue().set(EVENT_DONE_KEY + instructionId, "1", ttlSeconds, TimeUnit.SECONDS);
        redisTemplate.delete(EVENT_PROCESSING_KEY + instructionId);
    }

    public void releaseEventLock(String instructionId) {
        redisTemplate.delete(EVENT_PROCESSING_KEY + instructionId);
    }
}
