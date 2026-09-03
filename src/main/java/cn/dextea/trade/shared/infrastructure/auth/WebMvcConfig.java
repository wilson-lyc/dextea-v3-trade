package cn.dextea.trade.shared.infrastructure.auth;

import cn.dextea.trade.shared.config.AuthConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/docs/**",
                        "/docs/ui/**",
                        "/docs/json/**",
                        "/actuator/**",
                        "/error",
                        "/favicon.ico"
                );
    }
}
