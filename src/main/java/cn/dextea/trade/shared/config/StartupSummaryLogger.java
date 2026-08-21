package cn.dextea.trade.shared.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.sql.Connection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 启动完成后输出访问地址与基础设施连接状态总结。
 * <p>
 * MySQL / Redis 走真实连接探测，Nacos / RocketMQ 走 TCP 探测；
 * 各项并行执行且单项限时，避免某个组件不可用时阻塞启动。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StartupSummaryLogger {

    private static final String OK = "已连接";
    private static final String FAIL = "连接失败";
    private static final String TIMEOUT = "连接超时";
    private static final String OFF = "未启用";

    /** 单项探测等待上限（秒） */
    private static final long PROBE_TIMEOUT_SECONDS = 5;
    /** TCP 探测超时（毫秒） */
    private static final int TCP_TIMEOUT_MILLIS = 2000;

    private final Environment env;
    private final DataSource dataSource;
    private final StringRedisTemplate stringRedisTemplate;

    private record Probe(String label, Callable<String> task) {
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onReady(ApplicationReadyEvent event) {
        String contextPath = env.getProperty("server.servlet.context-path", "");
        int port = event.getApplicationContext() instanceof ServletWebServerApplicationContext webContext
                ? webContext.getWebServer().getPort()
                : Integer.parseInt(env.getProperty("server.port", "9090"));
        String base = "http://localhost:" + port + contextPath;

        Probe[] probes = {
                new Probe("MySQL", this::checkMysql),
                new Probe("Redis", this::checkRedis),
                new Probe("Nacos", this::checkNacos),
                new Probe("支付回调MQ", () -> checkMq("payment-callback-mq")),
                new Probe("制作状态MQ", () -> checkMq("order-making-mq")),
                new Probe("超时关单MQ", () -> checkMq("order-timeout-mq")),
                new Probe("OTel", this::checkOtel),
        };

        ExecutorService executor = Executors.newFixedThreadPool(Math.min(probes.length, 4), r -> {
            Thread t = new Thread(r, "startup-summary-probe");
            t.setDaemon(true);
            return t;
        });
        Map<String, Future<String>> futures = new LinkedHashMap<>();
        try {
            for (Probe probe : probes) {
                futures.put(probe.label(), executor.submit(probe.task()));
            }
            log.info("------------------------------------------------------------");
            log.info("  dextea-trade 启动完成");
            log.info("  {}{}", pad("控制台"), base + "/console/");
            if (docsEnabled()) {
                log.info("  {}{}{}", pad("接口文档"), base, env.getProperty("springdoc.swagger-ui.path", "/docs/ui"));
            }
            for (Probe probe : probes) {
                log.info("  {}{}", pad(probe.label()), statusOf(futures.get(probe.label())));
            }
            log.info("------------------------------------------------------------");
        } finally {
            executor.shutdownNow();
        }
    }

    private boolean docsEnabled() {
        return env.getProperty("springdoc.api-docs.enabled", Boolean.class, true)
                && env.getProperty("springdoc.swagger-ui.enabled", Boolean.class, true);
    }

    private String statusOf(Future<String> future) {
        try {
            return future.get(PROBE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            return TIMEOUT;
        } catch (Exception e) {
            return FAIL;
        }
    }

    private String checkMysql() {
        try (Connection conn = dataSource.getConnection()) {
            return conn.isValid(3) ? OK : FAIL;
        } catch (Exception e) {
            return FAIL;
        }
    }

    private String checkRedis() {
        try {
            return Boolean.TRUE.equals(stringRedisTemplate.execute(
                    (RedisCallback<Boolean>) conn -> conn.ping() != null)) ? OK : FAIL;
        } catch (Exception e) {
            return FAIL;
        }
    }

    private String checkNacos() {
        boolean discoveryEnabled = env.getProperty("spring.nacos.discovery.enabled", Boolean.class, false);
        // 未显式配置 Nacos 地址且注册中心未开启时视为未启用，避免默认 localhost 探测噪音
        if (!discoveryEnabled && env.getProperty("NACOS_SERVER_ADDR") == null) {
            return OFF;
        }
        String serverAddr = env.getProperty("spring.nacos.config.server-addr",
                env.getProperty("spring.nacos.discovery.server-addr", "127.0.0.1:8848"));
        return reachable(serverAddr) ? OK : FAIL;
    }

    private String checkMq(String prefix) {
        if (!env.getProperty(prefix + ".enabled", Boolean.class, false)) {
            return OFF;
        }
        return reachable(env.getProperty(prefix + ".endpoints")) ? OK : FAIL;
    }

    private String checkOtel() {
        if (!env.getProperty("otel.enabled", Boolean.class, true) || allExportersNone()) {
            return OFF;
        }
        return reachable(otlpEndpoint()) ? OK : FAIL;
    }

    private boolean allExportersNone() {
        return "none".equalsIgnoreCase(env.getProperty("OTEL_TRACES_EXPORTER", "otlp"))
                && "none".equalsIgnoreCase(env.getProperty("OTEL_LOGS_EXPORTER", "otlp"))
                && "none".equalsIgnoreCase(env.getProperty("OTEL_METRICS_EXPORTER", "otlp"));
    }

    /**
     * 解析 OTLP 上报地址，与 SDK 自动配置的取值顺序保持一致：
     * 信号级覆盖 > 通用 OTEL_EXPORTER_OTLP_ENDPOINT > 协议对应默认端口。
     */
    private String otlpEndpoint() {
        String endpoint = env.getProperty("OTEL_EXPORTER_OTLP_TRACES_ENDPOINT");
        if (endpoint == null) {
            endpoint = env.getProperty("OTEL_EXPORTER_OTLP_ENDPOINT");
        }
        if (endpoint == null) {
            String protocol = env.getProperty("OTEL_EXPORTER_OTLP_PROTOCOL", "grpc");
            endpoint = protocol.startsWith("http") ? "http://localhost:4318" : "http://localhost:4317";
        }
        return endpoint;
    }

    /**
     * 探测地址列表（逗号或分号分隔的 host:port，容忍 http 前缀与路径部分）是否至少有一个可达。
     */
    private boolean reachable(String addressList) {
        if (addressList == null || addressList.isBlank()) {
            return false;
        }
        for (String item : addressList.split("[,;]")) {
            String addr = item.trim().replaceFirst("^https?://", "");
            int slash = addr.indexOf('/');
            if (slash >= 0) {
                addr = addr.substring(0, slash);
            }
            int idx = addr.lastIndexOf(':');
            if (idx <= 0) {
                continue;
            }
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(addr.substring(0, idx),
                        Integer.parseInt(addr.substring(idx + 1))), TCP_TIMEOUT_MILLIS);
                return true;
            } catch (Exception ignored) {
                // 单个地址不通则尝试下一个
            }
        }
        return false;
    }

    /** 按显示宽度（中文按 2 格）补齐标签，保证日志纵向对齐 */
    private String pad(String label) {
        int width = label.chars().map(c -> c > 127 ? 2 : 1).sum();
        return label + " ".repeat(Math.max(1, 14 - width));
    }
}
