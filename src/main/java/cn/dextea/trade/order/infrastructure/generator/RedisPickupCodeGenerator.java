package cn.dextea.trade.order.infrastructure.generator;

import cn.dextea.trade.order.domain.exception.OrderErrorCode;
import cn.dextea.trade.order.domain.port.PickupCodeGenerator;
import cn.dextea.trade.shared.domain.error.BizError;
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
public class RedisPickupCodeGenerator implements PickupCodeGenerator {

    private static final String KEY_PREFIX = "dextea:order:pickup:";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final Duration KEY_TTL = Duration.ofHours(24);

    private final StringRedisTemplate redisTemplate;

    @Override
    public String next(Long storeId) {
        String key = KEY_PREFIX + storeId + ":" + LocalDate.now().format(DATE_FORMATTER);
        Long count;
        try {
            count = redisTemplate.opsForValue().increment(key);
        } catch (RuntimeException e) {
            throw new BizError(OrderErrorCode.PICKUP_CODE_GENERATE_FAILED, "取餐码计数自增失败: key=" + key, e);
        }
        if (count == null) {
            throw new BizError(OrderErrorCode.PICKUP_CODE_GENERATE_FAILED, "取餐码计数自增失败: key=" + key);
        }
        if (count == 1L) {
            redisTemplate.expire(key, KEY_TTL);
        }
        String pickupCode = "8" + String.format("%03d", count % 1000);
        log.debug("生成取餐码: storeId={}, count={}, pickupCode={}", storeId, count, pickupCode);
        return pickupCode;
    }
}
