package cn.dextea.trade.mq;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.apis.ClientConfiguration;
import org.apache.rocketmq.client.apis.ClientConfigurationBuilder;
import org.apache.rocketmq.client.apis.ClientException;
import org.apache.rocketmq.client.apis.ClientServiceProvider;
import org.apache.rocketmq.client.apis.SessionCredentialsProvider;
import org.apache.rocketmq.client.apis.StaticSessionCredentialsProvider;
import org.apache.rocketmq.client.apis.consumer.ConsumeResult;
import org.apache.rocketmq.client.apis.consumer.FilterExpression;
import org.apache.rocketmq.client.apis.consumer.FilterExpressionType;
import org.apache.rocketmq.client.apis.consumer.PushConsumer;
import org.apache.rocketmq.client.apis.message.MessageView;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

/**
 * 基于 RocketMQ 5.x Java SDK PushConsumer 的支付回单消费者。
 *
 * <p>使用 push 模式，由 SDK 管理消费并发度与消息分发，业务仅需实现监听器回调处理消息：
 * <ul>
 *     <li>消息体反序列化为 {@link PaymentNotifyMessage} 后交给 {@link PaymentNotifyService} 处理；</li>
 *     <li>处理成功返回 {@link ConsumeResult#SUCCESS} 确认消息；</li>
 *     <li>消息体无法解析（不可恢复）直接确认，避免毒消息阻塞队列；</li>
 *     <li>业务处理抛出瞬时异常返回 {@link ConsumeResult#FAILURE} 触发服务端重试。</li>
 * </ul>
 * </p>
 *
 * <p>PushConsumer 在后台线程消费，无需阻塞主线程；通过 {@link PreDestroy} 在应用关停时优雅关闭。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentNotifyConsumer {

    private final RocketMqProperties properties;
    private final PaymentNotifyService paymentNotifyService;
    private final ObjectMapper objectMapper;

    private PushConsumer pushConsumer;

    @PostConstruct
    public void start() {
        if (!properties.isEnabled()) {
            log.info("RocketMQ 消费端未启用（rocketmq.enabled=false），跳过启动");
            return;
        }
        validateConfig();

        ClientServiceProvider provider = ClientServiceProvider.loadService();
        ClientConfigurationBuilder builder = ClientConfiguration.newBuilder()
                .setEndpoints(properties.getEndpoints());
        // 公网访问实例时需设置实例 ID（namespace）
        if (StringUtils.hasText(properties.getNamespace())) {
            builder.setNamespace(properties.getNamespace());
        }
        // 公网访问时需设置访问凭证（用户名密码）
        if (StringUtils.hasText(properties.getAccessKey()) && StringUtils.hasText(properties.getSecretKey())) {
            SessionCredentialsProvider credentialsProvider =
                    new StaticSessionCredentialsProvider(properties.getAccessKey(), properties.getSecretKey());
            builder.setCredentialProvider(credentialsProvider);
        }
        ClientConfiguration clientConfiguration = builder.build();

        FilterExpression filterExpression = new FilterExpression(
                StringUtils.hasText(properties.getTag()) ? properties.getTag() : "*",
                FilterExpressionType.TAG);

        try {
            pushConsumer = provider.newPushConsumerBuilder()
                    .setClientConfiguration(clientConfiguration)
                    .setConsumerGroup(properties.getConsumerGroup())
                    .setSubscriptionExpressions(Collections.singletonMap(properties.getTopic(), filterExpression))
                    .setMessageListener(this::handle)
                    .build();
            log.info("RocketMQ PushConsumer 已启动，topic={}, consumerGroup={}",
                    properties.getTopic(), properties.getConsumerGroup());
        } catch (ClientException e) {
            throw new IllegalStateException("初始化 RocketMQ PushConsumer 失败", e);
        }
    }

    private ConsumeResult handle(MessageView messageView) {
        String msgId = String.valueOf(messageView.getMessageId());
        try {
            ByteBuffer bodyBuf = messageView.getBody();
            byte[] bodyBytes = new byte[bodyBuf.remaining()];
            bodyBuf.get(bodyBytes);
            String body = new String(bodyBytes, StandardCharsets.UTF_8);
            PaymentNotifyMessage message = objectMapper.readValue(body, PaymentNotifyMessage.class);
            paymentNotifyService.handleNotify(message);
            return ConsumeResult.SUCCESS;
        } catch (IOException e) {
            // 消息体无法解析为约定格式，属于不可恢复消息，确认以免阻塞队列
            log.error("支付回单消息体解析失败，确认消息以免阻塞队列: msgId={}", msgId, e);
            return ConsumeResult.SUCCESS;
        } catch (Exception e) {
            // 业务处理异常（如数据库瞬时不可用），返回失败触发服务端重试
            log.error("支付回单处理失败，触发重试: msgId={}", msgId, e);
            return ConsumeResult.FAILURE;
        }
    }

    private void validateConfig() {
        if (!StringUtils.hasText(properties.getEndpoints())
                || !StringUtils.hasText(properties.getTopic())
                || !StringUtils.hasText(properties.getConsumerGroup())) {
            throw new IllegalStateException("RocketMQ 配置不完整：endpoints/topic/consumerGroup 均为必填");
        }
    }

    @PreDestroy
    public void stop() {
        if (pushConsumer != null) {
            try {
                pushConsumer.close();
                log.info("RocketMQ PushConsumer 已关闭");
            } catch (IOException e) {
                log.warn("关闭 RocketMQ PushConsumer 失败", e);
            }
        }
    }
}
