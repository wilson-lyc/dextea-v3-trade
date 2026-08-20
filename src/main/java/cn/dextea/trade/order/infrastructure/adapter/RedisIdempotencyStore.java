package cn.dextea.trade.order.infrastructure.adapter;

import cn.dextea.trade.order.domain.port.IdempotencyStore;
import cn.dextea.trade.shared.error.CommonErrorCode;
import cn.dextea.trade.shared.error.SystemException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisIdempotencyStore implements IdempotencyStore {

    private static final String KEY_PREFIX = "dextea:idem:create_order:";

    private final StringRedisTemplate stringRedisTemplate;

    @Value("${order.create_order_idem_ttl:1440}")
    private long idempotencyTtlMinutes;

    @Override
    public boolean exists(String idempotencyKey) {
        try {
            return Boolean.TRUE.equals(stringRedisTemplate.hasKey(buildKey(idempotencyKey)));
        } catch (RuntimeException e) {
            log.error("幂等键查询失败, 视为不存在以确保安全, key={}", idempotencyKey, e);
            throw new SystemException(CommonErrorCode.DB_NOT_ENABLED, "幂等存储不可用", e);
        }
    }

    @Override
    public void record(String idempotencyKey, String orderNo) {
        try {
            stringRedisTemplate.opsForValue().set(buildKey(idempotencyKey), orderNo, Duration.ofMinutes(idempotencyTtlMinutes));
        } catch (RuntimeException e) {
            log.error("幂等键写入失败, key={}", idempotencyKey, e);
            throw new SystemException(CommonErrorCode.DB_NOT_ENABLED, "幂等存储写入失败", e);
        }
    }

    private String buildKey(String idempotencyKey) {
        return KEY_PREFIX + idempotencyKey;
    }
}
