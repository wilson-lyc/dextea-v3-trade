package cn.dextea.trade.shared.infrastructure.auth;

import cn.dextea.trade.shared.config.AuthConfig;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 合法 API 令牌集合，来源为环境变量 AUTH_TOKENS（逗号分隔）。
 * 服务启动时加载进内存 HashMap，校验时命中任一令牌即通过。
 */
@Component
@RequiredArgsConstructor
public class ApiTokenStore {

    private final AuthConfig authConfig;

    private Map<String, Boolean> tokenMap = Map.of();

    @PostConstruct
    public void init() {
        Map<String, Boolean> map = new HashMap<>();
        if (authConfig.getTokens() != null) {
            for (String token : authConfig.getTokens()) {
                if (token != null && !token.isBlank()) {
                    map.put(token.trim(), Boolean.TRUE);
                }
            }
        }
        tokenMap = map;
    }

    public boolean contains(String token) {
        return tokenMap.containsKey(token);
    }
}
