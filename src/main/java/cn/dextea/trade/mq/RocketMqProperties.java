package cn.dextea.trade.mq;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "rocketmq")
public class RocketMqProperties {

    /** 实例接入点，格式为 host:port */
    private String endpoints;

    /** 订阅的 Topic 名称 */
    private String topic;

    /** 消费者分组（ConsumerGroup） */
    private String consumerGroup;

    /** 订阅 Tag 过滤规则，默认 "*" 表示订阅所有 Tag */
    private String tag = "*";

    /** 公网访问实例时填写的实例 ID（namespace） */
    private String namespace;

    /** 公网访问用户名（控制台获取） */
    private String accessKey;

    /** 公网访问密码（控制台获取） */
    private String secretKey;
}
