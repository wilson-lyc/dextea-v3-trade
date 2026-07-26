package cn.dextea.trade.pay.interfaces.mq;

import cn.dextea.trade.pay.application.PaymentCallbackService;
import cn.dextea.trade.pay.infrastructure.config.RocketMqConfig;
import cn.dextea.trade.pay.interfaces.dto.PaymentCallbackMessage;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

/**
 * 支付回单 RocketMQ 消费入口。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentCallbackConsumer {

    private final RocketMqConfig properties;
    private final PaymentCallbackService paymentCallbackService;
    private final ObjectMapper objectMapper;

    @Value("${spring.application.name:dextea-trade}")
    private String applicationName;

    private PushConsumer pushConsumer;

    @PostConstruct
    public void start() {
        validateConfig();

        ClientServiceProvider provider = ClientServiceProvider.loadService();
        ClientConfigurationBuilder builder = ClientConfiguration.newBuilder()
                .setEndpoints(properties.getEndpoints());
        if (StringUtils.hasText(properties.getNamespace())) {
            builder.setNamespace(properties.getNamespace());
        }
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
            PaymentCallbackMessage message = objectMapper.readValue(body, PaymentCallbackMessage.class);
            paymentCallbackService.handleCallback(message);
            return ConsumeResult.SUCCESS;
        } catch (IOException e) {
            log.error("支付回单消息体解析失败，确认消息以免阻塞队列: msgId={}", msgId, e);
            return ConsumeResult.SUCCESS;
        } catch (Exception e) {
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
