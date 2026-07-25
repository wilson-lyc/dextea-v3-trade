package cn.dextea.trade.mq;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * RocketMQ 消费端配置绑定。
 *
 * <p>所有配置项均以 {@code rocketmq.*} 前缀注入，支持环境变量覆盖（如 {@code ROCKETMQ_ENDPOINTS}）。
 * 公网访问实例时，需补充 {@code namespace}（实例 ID）与 {@code access-key}/{@code secret-key}（访问凭证）。</p>
 *
 * <p>本地/测试环境默认 {@code enabled=false}，避免误连实例；生产环境通过环境变量 {@code ROCKETMQ_ENABLED=true} 开启。</p>
 */
@Data
@Component
@ConfigurationProperties(prefix = "rocketmq")
public class RocketMqProperties {

    /** 是否启动消费端，默认关闭 */
    private boolean enabled = false;

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
