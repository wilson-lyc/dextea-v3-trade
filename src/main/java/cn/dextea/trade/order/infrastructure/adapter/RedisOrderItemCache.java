package cn.dextea.trade.order.infrastructure.adapter;

import cn.dextea.trade.order.infrastructure.persistence.po.OrderItemPO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisOrderItemCache {

    private static final String KEY_PREFIX = "dextea:cache:order_item:";

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${order.order_item_cache_ttl:120}")
    private long cacheTtlMinutes;

    public List<OrderItemPO> get(Long orderId) {
        try {
            String json = stringRedisTemplate.opsForValue().get(buildKey(orderId));
            return deserialize(json);
        } catch (Exception e) {
            log.warn("读取订单项缓存失败, 降级查库, orderId={}", orderId, e);
            return null;
        }
    }

    public Map<Long, List<OrderItemPO>> getMulti(List<Long> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            List<String> keys = orderIds.stream().map(this::buildKey).toList();
            List<String> values = stringRedisTemplate.opsForValue().multiGet(keys);
            Map<Long, List<OrderItemPO>> result = new HashMap<>();
            for (int i = 0; i < orderIds.size(); i++) {
                List<OrderItemPO> items = deserialize(values == null ? null : values.get(i));
                if (items != null) {
                    result.put(orderIds.get(i), items);
                }
            }
            return result;
        } catch (Exception e) {
            log.warn("批量读取订单项缓存失败, 降级查库, orderIds={}", orderIds, e);
            return Collections.emptyMap();
        }
    }

    public void put(Long orderId, List<OrderItemPO> items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        try {
            String json = objectMapper.writeValueAsString(items);
            stringRedisTemplate.opsForValue().set(
                    buildKey(orderId), json, Duration.ofMinutes(cacheTtlMinutes));
        } catch (Exception e) {
            log.warn("写入订单项缓存失败, orderId={}", orderId, e);
        }
    }

    private List<OrderItemPO> deserialize(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<OrderItemPO>>() {
            });
        } catch (Exception e) {
            log.warn("反序列化订单项缓存失败, 将回源数据库", e);
            return null;
        }
    }

    private String buildKey(Long orderId) {
        return KEY_PREFIX + orderId;
    }
}
