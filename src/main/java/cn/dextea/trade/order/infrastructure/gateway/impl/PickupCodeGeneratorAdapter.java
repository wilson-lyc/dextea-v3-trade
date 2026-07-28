package cn.dextea.trade.order.infrastructure.gateway.impl;

import cn.dextea.trade.order.domain.gateway.PickupCodeGeneratorGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 取餐码生成适配器：实现 {@link PickupCodeGeneratorGateway}，
 * 基于 Redis 按「门店 ID + 当日日期」维度原子自增计数。
 *
 * <p>key 形如 {@code dextea:order:pickup:{storeId}:{yyyyMMdd}}，
 * INCR 首次调用返回 1（对应当日第 1 笔），并设置 2 天过期防止计数键堆积；
 * 取餐码 = "8" + (计数值 % 100 的 3 位零填充)，如计数 11 → "8011"。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PickupCodeGeneratorAdapter implements PickupCodeGeneratorGateway {

    private static final String KEY_PREFIX = "dextea:order:pickup:";

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    /** 计数键过期时长：跨越当日即可，2 天足够且避免堆积 */
    private static final Duration KEY_TTL = Duration.ofDays(2);

    private final StringRedisTemplate redisTemplate;

    @Override
    public String generate(Long storeId) {
        String key = KEY_PREFIX + storeId + ":" + LocalDate.now().format(DATE_FORMATTER);
        Long count = redisTemplate.opsForValue().increment(key);
        if (count == null) {
            // pipeline/事务场景下理论上可能为 null，正常调用不会发生
            throw new IllegalStateException("取餐码计数自增失败: key=" + key);
        }
        if (count == 1L) {
            redisTemplate.expire(key, KEY_TTL);
        }
        String pickupCode = "8" + String.format("%03d", count % 100);
        log.debug("生成取餐码: storeId={}, count={}, pickupCode={}", storeId, count, pickupCode);
        return pickupCode;
    }
}
