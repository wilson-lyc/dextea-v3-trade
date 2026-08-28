package cn.dextea.trade.shared.infrastructure.mq;

import org.apache.rocketmq.client.apis.ClientConfiguration;
import org.apache.rocketmq.client.apis.ClientConfigurationBuilder;
import org.apache.rocketmq.client.apis.ClientServiceProvider;
import org.apache.rocketmq.client.apis.StaticSessionCredentialsProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@EnableConfigurationProperties(RocketMqProperties.class)
public class RocketMqClientFactory {

    private final RocketMqProperties properties;

    public RocketMqClientFactory(RocketMqProperties properties) {
        this.properties = properties;
    }

    public boolean isEnabled() {
        return properties.isEnabled();
    }

    public ClientServiceProvider provider() {
        return ClientServiceProvider.loadService();
    }

    public ClientConfiguration clientConfiguration() {
        ClientConfigurationBuilder builder = ClientConfiguration.newBuilder()
                .setEndpoints(properties.getEndpoints())
                .setCredentialProvider(new StaticSessionCredentialsProvider(
                        properties.getAccessKey(), properties.getSecretKey()));
        String namespace = properties.getNamespace();
        if (namespace != null && !namespace.isBlank()) {
            builder.setNamespace(namespace);
        }
        return builder.build();
    }
}
