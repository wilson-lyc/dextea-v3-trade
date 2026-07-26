package cn.dextea.trade.pay.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 支付回单 RocketMQ 配置（配置前缀 rocketmq 保持不变）。
 */
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
