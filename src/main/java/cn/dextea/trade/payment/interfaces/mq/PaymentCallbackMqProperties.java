package cn.dextea.trade.payment.interfaces.mq;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "rocketmq.payment-callback-mq")
public class PaymentCallbackMqProperties {

    private boolean enabled;

    private String topic;

    private String consumerGroup;

    private String tag;
}
