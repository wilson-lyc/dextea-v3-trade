package cn.dextea.trade.order.infrastructure.adapter;

import cn.dextea.trade.order.domain.port.IdempotencyStore;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RedisIdempotencyStore implements IdempotencyStore {

    private static final String KEY_PREFIX = "dextea:idem:create_order:";

    private final StringRedisTemplate stringRedisTemplate;

    @Value("${order.create_order_idem_ttl:1440}")
    private long idempotencyTtlMinutes;

    @Override
    public boolean exists(String idempotencyKey) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(buildKey(idempotencyKey)));
    }

    @Override
    public void record(String idempotencyKey, String orderNo) {
        stringRedisTemplate.opsForValue().set(buildKey(idempotencyKey), orderNo, Duration.ofMinutes(idempotencyTtlMinutes));
    }

    private String buildKey(String idempotencyKey) {
        return KEY_PREFIX + idempotencyKey;
    }
}
