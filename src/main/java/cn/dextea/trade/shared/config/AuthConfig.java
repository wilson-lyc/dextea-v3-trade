package cn.dextea.trade.shared.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "auth")
public class AuthConfig {
    private boolean enabled = false;
    private Console console = new Console();

    @Getter
    @Setter
    public static class Console {
        private String username = "admin";
        private String password = "admin";
    }
}
