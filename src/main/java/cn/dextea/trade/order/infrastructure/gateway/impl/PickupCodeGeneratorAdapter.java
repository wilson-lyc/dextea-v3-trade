package cn.dextea.trade.order.infrastructure.gateway.impl;
import cn.dextea.trade.order.domain.gateway.PickupCodeGeneratorGateway;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
@Slf4j
@Component
@RequiredArgsConstructor
public class PickupCodeGeneratorAdapter implements PickupCodeGeneratorGateway {
    private static final String KEY_PREFIX = "dextea:order:pickup:";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final Duration KEY_TTL = Duration.ofDays(2);
    private final StringRedisTemplate redisTemplate;
    @Override
    public String generate(Long storeId) {
        String key = KEY_PREFIX + storeId + ":" + LocalDate.now().format(DATE_FORMATTER);
        Long count = redisTemplate.opsForValue().increment(key);
        if (count == null) {
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
