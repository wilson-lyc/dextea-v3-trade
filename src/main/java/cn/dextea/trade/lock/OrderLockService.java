package cn.dextea.trade.lock;

import cn.dextea.trade.exception.BizError;
import cn.dextea.trade.error.OrderErrorCode;
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

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderLockService {

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

    /**
     * 在订单锁保护下执行业务逻辑。
     *
     * @param orderNo  订单号（作为锁粒度 key 的一部分）
     * @param supplier 业务逻辑
     * @param <T>      返回类型
     * @return 业务逻辑返回值
     * @throws BizError 获取锁失败时抛出 {@link OrderErrorCode#ORDER_LOCK_BUSY}
     */
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

    /**
     * 尝试获取锁：循环重试 {@link #WAIT_SECONDS} 秒，每次间隔 100ms。
     * <p>使用 {@code setIfAbsent(key, value, Duration)} 等价于 {@code SET NX EX}，保证原子性。</p>
     *
     * @param lockKey   锁键
     * @param lockValue 锁值（唯一标识，用于安全释放）
     * @return 是否成功获取锁
     */
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

    /**
     * 安全释放锁：通过 Lua 脚本校验 value 后删除，避免误删其他线程持有的锁。
     *
     * @param lockKey   锁键
     * @param lockValue 锁值
     */
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
