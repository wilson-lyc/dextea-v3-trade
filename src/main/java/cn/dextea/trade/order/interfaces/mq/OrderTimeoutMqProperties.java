package cn.dextea.trade.order.interfaces.mq;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "order-timeout-mq")
public class OrderTimeoutMqProperties {

    private boolean enabled;
    private boolean producerEnabled = true;
    private boolean consumerEnabled = true;
    private String endpoints;
    private String namespace;
    private String accessKey;
    private String secretKey;
    private String topic;
    private String consumerGroup;
    private String tag;
    private long delayMinutes;

    public boolean isProducerActive() {
        return enabled && producerEnabled;
    }

    public boolean isConsumerActive() {
        return enabled && consumerEnabled;
    }
}
