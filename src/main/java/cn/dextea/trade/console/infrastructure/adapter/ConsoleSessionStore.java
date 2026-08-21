package cn.dextea.trade.console.infrastructure.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ConsoleSessionStore {

    private static final String KEY_PREFIX = "dextea:auth:console:session:";
    private static final Duration TTL = Duration.ofHours(2);

    private final StringRedisTemplate stringRedisTemplate;

    public String create() {
        String session = UUID.randomUUID().toString().replace("-", "");
        stringRedisTemplate.opsForValue().set(buildKey(session), "1", TTL);
        return session;
    }

    public boolean valid(String session) {
        if (session == null || session.isBlank()) {
            return false;
        }
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(buildKey(session)));
    }

    public void refresh(String session) {
        stringRedisTemplate.expire(buildKey(session), TTL);
    }

    public void destroy(String session) {
        if (session == null || session.isBlank()) {
            return;
        }
        stringRedisTemplate.delete(buildKey(session));
    }

    private String buildKey(String session) {
        return KEY_PREFIX + session;
    }
}
