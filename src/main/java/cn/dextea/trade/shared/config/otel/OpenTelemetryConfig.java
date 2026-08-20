package cn.dextea.trade.shared.config.otel;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdkBuilder;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class OpenTelemetryConfig {

    @Value("${otel.enabled:true}")
    private boolean enabled;

    @Value("${otel.logs-export-enabled:true}")
    private boolean logsExportEnabled;

    private AutoConfiguredOpenTelemetrySdk autoConfiguredSdk;

    @Bean
    public OpenTelemetry openTelemetry() {
        if (!enabled) {
            log.warn("OpenTelemetry 未启用(otel.enabled=false)，使用无操作实现");
            return OpenTelemetry.noop();
        }
        AutoConfiguredOpenTelemetrySdkBuilder builder = AutoConfiguredOpenTelemetrySdk.builder();
        autoConfiguredSdk = builder.build();
        OpenTelemetry openTelemetry = autoConfiguredSdk.getOpenTelemetrySdk();
        GlobalOpenTelemetry.set(openTelemetry);
        log.info("OpenTelemetry 已按 OTEL_* 标准环境变量自动配置完成");
        log.info("日志 OTLP 上报{} (otel.logs-export-enabled={})",
                logsExportEnabled ? "已开启" : "已关闭", logsExportEnabled);
        return openTelemetry;
    }

    @PreDestroy
    public void destroy() {
        if (autoConfiguredSdk != null) {
            autoConfiguredSdk.getOpenTelemetrySdk().getSdkTracerProvider().close();
            autoConfiguredSdk.getOpenTelemetrySdk().getSdkLoggerProvider().close();
            autoConfiguredSdk.getOpenTelemetrySdk().getSdkMeterProvider().close();
        }
    }
}
