package cn.dextea.trade;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;

@Slf4j
@SpringBootApplication
public class DexteaTradeApplication {
    public static void main(String[] args) {
        SpringApplication.run(DexteaTradeApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onReady(ApplicationReadyEvent event) {
        Environment env = event.getApplicationContext().getEnvironment();
        String contextPath = env.getProperty("server.servlet.context-path", "");
        int port = event.getApplicationContext() instanceof ServletWebServerApplicationContext webContext
                ? webContext.getWebServer().getPort()
                : Integer.parseInt(env.getProperty("server.port", "9090"));
        String base = "http://localhost:" + port + contextPath;

        log.info("------------------------------------------------------------");
        log.info("  dextea-trade 启动完成");
        log.info("  控制台     {}", base + "/console/");
        boolean docsEnabled = env.getProperty("springdoc.api-docs.enabled", Boolean.class, true)
                && env.getProperty("springdoc.swagger-ui.enabled", Boolean.class, true);
        if (docsEnabled) {
            log.info("  接口文档   {}{}", base, env.getProperty("springdoc.swagger-ui.path", "/docs/ui"));
        }
        log.info("------------------------------------------------------------");
    }
}
