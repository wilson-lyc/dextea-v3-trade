package cn.dextea.trade.service.impl;

import cn.dextea.trade.model.HealthResult;
import cn.dextea.trade.service.HealthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class HealthServiceImpl implements HealthService {

    private final DataSource dataSource;
    private final RedisConnectionFactory redisConnectionFactory;

    @Value("${spring.application.name:dextea-trade}")
    private String applicationName;

    @Override
    public HealthResult checkMysql() {
        long start = System.currentTimeMillis();
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT 1")) {
            boolean ok = rs.next() && rs.getInt(1) == 1;
            long cost = System.currentTimeMillis() - start;
            if (ok) {
                Map<String, Object> details = new LinkedHashMap<>();
                details.put("url", conn.getMetaData().getURL());
                details.put("database", conn.getCatalog());
                return HealthResult.up("mysql", "MySQL 连接正常", cost, details);
            }
            return HealthResult.down("mysql", "MySQL 返回结果异常", cost, null);
        } catch (Exception e) {
            long cost = System.currentTimeMillis() - start;
            log.warn("MySQL 健康检查失败: {}", e.getMessage());
            Map<String, Object> details = new LinkedHashMap<>();
            details.put("error", e.getClass().getSimpleName() + ": " + e.getMessage());
            return HealthResult.down("mysql", "MySQL 连接不可用", cost, details);
        }
    }

    @Override
    public HealthResult checkRedis() {
        long start = System.currentTimeMillis();
        try (RedisConnection conn = redisConnectionFactory.getConnection()) {
            String pong = conn.ping();
            long cost = System.currentTimeMillis() - start;
            boolean ok = pong != null && "PONG".equalsIgnoreCase(pong);
            if (ok) {
                Map<String, Object> details = new LinkedHashMap<>();
                details.put("pong", pong);
                return HealthResult.up("redis", "Redis 连接正常", cost, details);
            }
            return HealthResult.down("redis", "Redis 返回结果异常", cost, null);
        } catch (Exception e) {
            long cost = System.currentTimeMillis() - start;
            log.warn("Redis 健康检查失败: {}", e.getMessage());
            Map<String, Object> details = new LinkedHashMap<>();
            details.put("error", e.getClass().getSimpleName() + ": " + e.getMessage());
            return HealthResult.down("redis", "Redis 连接不可用", cost, details);
        }
    }

    @Override
    public HealthResult checkBackend() {
        long start = System.currentTimeMillis();
        Runtime runtime = Runtime.getRuntime();
        long cost = System.currentTimeMillis() - start;
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("application", applicationName);
        details.put("availableProcessors", runtime.availableProcessors());
        details.put("freeMemory", runtime.freeMemory());
        details.put("maxMemory", runtime.maxMemory());
        return HealthResult.up("backend", "后端服务运行正常", cost, details);
    }
}
