package cn.dextea.trade.order.interfaces.mq;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "order-making-mq")
public class OrderMakingMqProperties {

    private boolean enabled;
    private String endpoints;
    private String namespace;
    private String accessKey;
    private String secretKey;
    private String topic;

    public boolean isActive() {
        return enabled;
    }
}
