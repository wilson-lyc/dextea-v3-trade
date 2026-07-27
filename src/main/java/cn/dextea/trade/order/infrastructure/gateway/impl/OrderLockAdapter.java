package cn.dextea.trade.order.infrastructure.gateway.impl;

import cn.dextea.trade.common.error.BizError;
import cn.dextea.trade.order.domain.exception.OrderErrorCode;
import cn.dextea.trade.order.domain.gateway.OrderLockGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * 订单分布式锁适配器：实现 {@link OrderLockGateway}，基于 Redis 实现可重入安全的分布式锁。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderLockAdapter implements OrderLockGateway {

    private final StringRedisTemplate redisTemplate;

    /** 加锁等待时间（秒）：最多阻塞 3 秒等待其他线程释放 */
    private static final long WAIT_SECONDS = 3;

    /** 锁持有时间（秒）：10 秒后自动过期，防止持锁线程崩溃导致死锁 */
    private static final long LEASE_SECONDS = 10;

    /** Lua 释放脚本：只有 value 匹配时才删除，避免误删别人的锁 */
    private static final RedisScript<Long> UNLOCK_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end",
            Long.class
    );

    @Override
    public <T> T executeWithLock(String orderNo, Supplier<T> supplier) {
        String lockKey = "dextea:order:lock:" + orderNo;
        String lockValue = UUID.randomUUID().toString();

        boolean acquired = tryAcquire(lockKey, lockValue);
        if (!acquired) {
            throw new BizError(OrderErrorCode.ORDER_LOCK_BUSY, "系统繁忙，请稍后重试");
        }

        try {
            return supplier.get();
        } finally {
            release(lockKey, lockValue);
        }
    }

    private boolean tryAcquire(String lockKey, String lockValue) {
        long deadline = System.currentTimeMillis() + WAIT_SECONDS * 1000;
        Duration lease = Duration.ofSeconds(LEASE_SECONDS);

        while (System.currentTimeMillis() < deadline) {
            Boolean ok = redisTemplate.opsForValue().setIfAbsent(lockKey, lockValue, lease);
            if (Boolean.TRUE.equals(ok)) {
                return true;
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new BizError(OrderErrorCode.ORDER_LOCK_BUSY, "加锁被中断");
            }
        }
        return false;
    }

    private void release(String lockKey, String lockValue) {
        try {
            Long result = redisTemplate.execute(UNLOCK_SCRIPT, List.of(lockKey), lockValue);
            if (result == null || result == 0) {
                log.debug("释放订单锁未命中（可能已过期或被其他线程持有）: key={}", lockKey);
            }
        } catch (Exception e) {
            log.warn("释放订单锁异常（不影响业务，锁会自动过期）: key={}, err={}", lockKey, e.getMessage());
        }
    }
}
