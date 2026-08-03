package cn.dextea.trade.order.infrastructure.adapter;

import cn.dextea.trade.order.domain.port.OrderCreateLock;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RedisOrderCreateLock implements OrderCreateLock {

    private static final String KEY_PREFIX = "dextea:lock:create_order:";

    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end",
            Long.class);

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean tryLock(String lockKey, String token, Duration duration) {
        return Boolean.TRUE.equals(stringRedisTemplate.opsForValue().setIfAbsent(buildKey(lockKey), token, duration));
    }

    @Override
    public void unlock(String lockKey, String token) {
        stringRedisTemplate.execute(UNLOCK_SCRIPT, List.of(buildKey(lockKey)), token);
    }

    private String buildKey(String lockKey) {
        return KEY_PREFIX + lockKey;
    }
}
