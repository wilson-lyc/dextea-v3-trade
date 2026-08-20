package cn.dextea.trade.shared.config.otel;

import io.opentelemetry.api.OpenTelemetry;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class OtelWebConfig implements WebMvcConfigurer {

    private final OpenTelemetry openTelemetry;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new TraceInterceptor(openTelemetry)).addPathPatterns("/api/**");
    }
}
