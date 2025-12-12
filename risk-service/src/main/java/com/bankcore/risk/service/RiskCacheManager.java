package com.bankcore.risk.service;

import com.bankcore.risk.model.RiskRule;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 风控缓存/计数组件：
 * 1) 缓存启用规则，减少 DB 压力；
 * 2) 通过 Redis 计数器做日累计、分钟频次统计，避免高并发下打爆数据库。
 */
@Component
public class RiskCacheManager {
    private static final Logger log = LoggerFactory.getLogger(RiskCacheManager.class);
    private static final String RULE_CACHE_KEY = "risk:rules:enabled";
    private static final String DAILY_AMOUNT_KEY = "risk:daily:amount:";
    private static final String FREQ_KEY = "risk:freq:";
    private static final long RULE_CACHE_TTL_SECONDS = 300L;
    private static final int SCALE = 2;

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RiskCacheManager(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 从 Redis 读取启用规则，缺失时通过 loader 查询数据库并缓存 5 分钟。
     */
    public List<RiskRule> loadEnabledRules(Supplier<List<RiskRule>> loader) {
        try {
            String cached = redisTemplate.opsForValue().get(RULE_CACHE_KEY);
            if (cached != null && cached.length() > 0) {
                return objectMapper.readValue(cached, new TypeReference<List<RiskRule>>() {
                });
            }
        } catch (Exception ex) {
            log.warn("读取规则缓存失败，降级为数据库查询", ex);
        }
        List<RiskRule> rules = loader.get();
        try {
            redisTemplate.opsForValue().set(RULE_CACHE_KEY, objectMapper.writeValueAsString(rules), RULE_CACHE_TTL_SECONDS, TimeUnit.SECONDS);
        } catch (Exception ex) {
            log.warn("规则写入缓存失败，不影响主流程", ex);
        }
        return rules == null ? Collections.<RiskRule>emptyList() : rules;
    }

    /**
     * 清空规则缓存，便于运营手工刷新。
     */
    public void evictRules() {
        redisTemplate.delete(RULE_CACHE_KEY);
    }

    /**
     * 累加当日交易金额（以分为单位），返回累加后的值；Redis 失效时返回 null。
     */
    public Long incrementDailyAmount(String customerId, BigDecimal amount) {
        if (customerId == null || amount == null) {
            return null;
        }
        try {
            String key = DAILY_AMOUNT_KEY + customerId + ":" + currentDate();
            long delta = amount.movePointRight(SCALE).longValue();
            Long total = redisTemplate.opsForValue().increment(key, delta);
            if (total != null) {
                redisTemplate.expire(key, secondsToEndOfDay(), TimeUnit.SECONDS);
            }
            return total;
        } catch (Exception ex) {
            log.warn("日累计计数写入 Redis 失败，customer={}", customerId, ex);
            return null;
        }
    }

    /**
     * 高频交易计数，按分钟粒度累加，返回当前分钟内的次数。
     */
    public Long incrementFrequency(String customerId) {
        if (customerId == null) {
            return null;
        }
        try {
            String key = FREQ_KEY + customerId + ":" + currentMinute();
            Long count = redisTemplate.opsForValue().increment(key, 1L);
            if (count != null) {
                redisTemplate.expire(key, 3600, TimeUnit.SECONDS);
            }
            return count;
        } catch (Exception ex) {
            log.warn("频次计数写入 Redis 失败，customer={}", customerId, ex);
            return null;
        }
    }

    private String currentDate() {
        LocalDateTime now = LocalDateTime.now();
        return String.format("%04d%02d%02d", now.getYear(), now.getMonthValue(), now.getDayOfMonth());
    }

    private String currentMinute() {
        LocalDateTime now = LocalDateTime.now();
        return String.format("%04d%02d%02d%02d%02d", now.getYear(), now.getMonthValue(), now.getDayOfMonth(), now.getHour(), now.getMinute());
    }

    private long secondsToEndOfDay() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime end = LocalDateTime.of(now.toLocalDate(), LocalTime.MAX);
        return Duration.between(now, end).getSeconds();
    }
}
