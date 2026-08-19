package cn.dextea.trade.payment.interfaces.mq;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "payment-callback-mq")
public class PaymentCallbackMqProperties {

    private boolean enabled;

    private String endpoints;

    private String namespace;

    private String accessKey;

    private String secretKey;

    private String topic;

    private String consumerGroup;

    private String tag;
}
