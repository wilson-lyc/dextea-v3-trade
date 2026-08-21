package cn.dextea.trade.console.infrastructure.adapter;

import cn.dextea.trade.console.infrastructure.persistence.po.ApiTokenPO;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Component
public class ApiTokenCache {

    private static final int MAX_SIZE = 10000;
    private static final Duration TTL = Duration.ofMinutes(5);

    private final Cache<String, Optional<ApiTokenPO>> cache = Caffeine.newBuilder()
            .maximumSize(MAX_SIZE)
            .expireAfterWrite(TTL)
            .build();

    public Optional<ApiTokenPO> get(String token) {
        return cache.getIfPresent(token);
    }

    public void put(ApiTokenPO po) {
        if (po != null) {
            cache.put(po.getToken(), Optional.of(po));
        }
    }

    public void putMissing(String token) {
        cache.put(token, Optional.empty());
    }

    public void evict(String token) {
        cache.invalidate(token);
    }
}
