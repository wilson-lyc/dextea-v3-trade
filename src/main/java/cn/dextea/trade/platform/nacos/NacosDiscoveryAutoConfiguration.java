package cn.dextea.trade.platform.nacos;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;

@Configuration
@EnableConfigurationProperties(NacosDiscoveryProperties.class)
@ConditionalOnProperty(prefix = "spring.nacos.discovery", name = "enabled", havingValue = "true")
public class NacosDiscoveryAutoConfiguration {

    @Bean
    public NacosDiscoveryRegistrar nacosDiscoveryRegistrar(NacosDiscoveryProperties properties) {
        return new NacosDiscoveryRegistrar(properties);
    }

    @EventListener(org.springframework.boot.context.event.ApplicationReadyEvent.class)
    public void onReady(NacosDiscoveryRegistrar registrar) {
        registrar.register();
    }

    @EventListener(ContextClosedEvent.class)
    public void onClosed(NacosDiscoveryRegistrar registrar) {
        registrar.deregister();
    }
}
