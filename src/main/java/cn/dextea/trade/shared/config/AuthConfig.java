package cn.dextea.trade.shared.config;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "auth")
public class AuthConfig {
    private boolean enabled = false;
    private List<String> tokens = List.of();
}
