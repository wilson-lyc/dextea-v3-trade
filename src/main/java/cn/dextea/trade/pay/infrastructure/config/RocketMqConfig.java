package cn.dextea.trade.pay.infrastructure.config;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
@Data
@Component
@ConfigurationProperties(prefix = "rocketmq")
public class RocketMqConfig {
    private String endpoints;
    private String topic;
    private String consumerGroup;
    private String tag = "*";
    private String namespace;
    private String accessKey;
    private String secretKey;
}
