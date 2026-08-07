package cn.dextea.trade.pay.interfaces.mq;

import cn.dextea.trade.pay.application.dto.PaymentCallbackMessage;
import cn.dextea.trade.pay.application.service.PaymentCallbackApplicationService;
import cn.dextea.trade.shared.error.BizError;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.apis.ClientConfiguration;
import org.apache.rocketmq.client.apis.ClientConfigurationBuilder;
import org.apache.rocketmq.client.apis.ClientException;
import org.apache.rocketmq.client.apis.ClientServiceProvider;
import org.apache.rocketmq.client.apis.StaticSessionCredentialsProvider;
import org.apache.rocketmq.client.apis.consumer.FilterExpression;
import org.apache.rocketmq.client.apis.consumer.FilterExpressionType;
import org.apache.rocketmq.client.apis.consumer.SimpleConsumer;
import org.apache.rocketmq.client.apis.message.MessageView;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Configuration
@EnableConfigurationProperties(PaymentCallbackMqProperties.class)
public class PaymentCallbackMqConsumer {

    private static final int MAX_RECEIVE_NUM = 16;

    private static final Duration RECEIVE_TIMEOUT = Duration.ofSeconds(20);

    private static final int MAX_RETRY_TIMES = 5;

    private final PaymentCallbackMqProperties properties;
    private final PaymentCallbackApplicationService paymentCallbackApplicationService;
    private final ObjectMapper objectMapper;

    private SimpleConsumer consumer;
    private ExecutorService consumeExecutor;
    private volatile boolean running = false;
    private final Map<String, Integer> retryCounter = new ConcurrentHashMap<>();

    public PaymentCallbackMqConsumer(PaymentCallbackMqProperties properties,
                                     PaymentCallbackApplicationService paymentCallbackApplicationService,
                                     ObjectMapper objectMapper) {
        this.properties = properties;
        this.paymentCallbackApplicationService = paymentCallbackApplicationService;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void start() {
        if (!properties.isEnabled()) {
            log.info("payment-callback-mq 消费未启用，跳过消费者初始化");
            return;
        }
        try {
            this.consumer = buildConsumer();
            this.running = true;
            this.consumeExecutor = Executors.newSingleThreadExecutor(r -> new Thread(r, "payment-callback-mq-consumer"));
            this.consumeExecutor.submit(this::consumeLoop);
            log.info("payment-callback-mq 消费者启动成功, topic={}, consumerGroup={}, tag={}",
                    properties.getTopic(), properties.getConsumerGroup(), properties.getTag());
        } catch (Exception e) {
            throw new IllegalStateException("payment-callback-mq 消费者初始化失败", e);
        }
    }

    private SimpleConsumer buildConsumer() throws ClientException {
        ClientServiceProvider provider = ClientServiceProvider.loadService();

        ClientConfigurationBuilder configBuilder = ClientConfiguration.newBuilder()
                .setEndpoints(properties.getEndpoints())
                .setCredentialProvider(new StaticSessionCredentialsProvider(
                        properties.getAccessKey(), properties.getSecretKey()));
        if (hasText(properties.getNamespace())) {
            configBuilder.setNamespace(properties.getNamespace());
        }

        FilterExpression filterExpression = new FilterExpression(properties.getTag(), FilterExpressionType.TAG);
        return provider.newSimpleConsumerBuilder()
                .setClientConfiguration(configBuilder.build())
                .setConsumerGroup(properties.getConsumerGroup())
                .setSubscriptionExpressions(Collections.singletonMap(properties.getTopic(), filterExpression))
                .setAwaitDuration(RECEIVE_TIMEOUT)
                .build();
    }

    private void consumeLoop() {
        while (running) {
            try {
                List<MessageView> messages = consumer.receive(MAX_RECEIVE_NUM, RECEIVE_TIMEOUT);
                for (MessageView message : messages) {
                    processMessage(message);
                }
            } catch (Exception e) {
                log.error("payment-callback 消息拉取异常", e);
                sleepQuietly(Duration.ofSeconds(1));
            }
        }
    }

    private void processMessage(MessageView message) {
        String messageId = String.valueOf(message.getMessageId());
        try {
            handleMessage(message);
            ackQuietly(message, messageId);
            retryCounter.remove(messageId);
        } catch (NonRetryableException | BizError e) {
            log.error("payment-callback 消息不可重试, 直接确认避免死循环, messageId={}, reason={}",
                    messageId, e.getMessage());
            ackQuietly(message, messageId);
        } catch (Exception e) {
            int times = retryCounter.merge(messageId, 1, Integer::sum);
            if (times >= MAX_RETRY_TIMES) {
                log.error("payment-callback 消息重试次数耗尽, 转死信, messageId={}", messageId, e);
                ackQuietly(message, messageId);
            } else {
                log.warn("payment-callback 消息处理失败, 等待 RocketMQ 重新投递, messageId={}, retryTimes={}/{}",
                        messageId, times, MAX_RETRY_TIMES, e);
            }
        }
    }

    private void ackQuietly(MessageView message, String messageId) {
        try {
            consumer.ack(message);
            retryCounter.remove(messageId);
        } catch (Exception e) {
            log.warn("payment-callback 消息确认失败, messageId={}", messageId, e);
        }
    }

    private void handleMessage(MessageView message) {
        ByteBuffer body = message.getBody();
        byte[] bytes = new byte[body.remaining()];
        body.get(bytes);
        PaymentCallbackMessage callbackMessage;
        try {
            callbackMessage = objectMapper.readValue(bytes, PaymentCallbackMessage.class);
        } catch (Exception e) {
            throw new NonRetryableException("支付回调消息反序列化失败", e);
        }
        log.info("收到支付回调消息, messageId={}, topic={}, platform={}, data={}",
                message.getMessageId(), message.getTopic(), callbackMessage.platform(), callbackMessage.data());
        paymentCallbackApplicationService.handle(callbackMessage);
    }

    private static final class NonRetryableException extends RuntimeException {
        NonRetryableException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    @PreDestroy
    public void stop() {
        this.running = false;
        if (this.consumeExecutor != null) {
            this.consumeExecutor.shutdown();
        }
        if (this.consumer != null) {
            try {
                this.consumer.close();
            } catch (Exception e) {
                log.warn("关闭 payment-callback-mq 消费者失败", e);
            }
        }
        log.info("payment-callback-mq 消费者已关闭");
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private void sleepQuietly(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
