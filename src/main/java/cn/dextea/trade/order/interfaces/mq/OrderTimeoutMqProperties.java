package cn.dextea.trade.order.interfaces.mq;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "rocketmq.order-timeout-mq")
public class OrderTimeoutMqProperties {

    private boolean enabled;

    private String topic;

    private String consumerGroup;

    private long delayMinutes;
}
