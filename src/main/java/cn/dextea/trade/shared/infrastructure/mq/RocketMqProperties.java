package cn.dextea.trade.shared.infrastructure.mq;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "rocketmq")
public class RocketMqProperties {

    private boolean enabled = true;

    private String endpoints;

    private String namespace;

    private String accessKey;

    private String secretKey;
}
