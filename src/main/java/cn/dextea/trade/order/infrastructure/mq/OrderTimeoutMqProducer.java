package cn.dextea.trade.order.infrastructure.mq;

import cn.dextea.trade.order.application.dto.OrderTimeoutMessage;
import cn.dextea.trade.order.domain.model.Order;
import cn.dextea.trade.order.domain.port.OrderTimeoutDelayPort;
import cn.dextea.trade.order.interfaces.mq.OrderTimeoutMqProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.apis.ClientConfiguration;
import org.apache.rocketmq.client.apis.ClientConfigurationBuilder;
import org.apache.rocketmq.client.apis.ClientException;
import org.apache.rocketmq.client.apis.ClientServiceProvider;
import org.apache.rocketmq.client.apis.StaticSessionCredentialsProvider;
import org.apache.rocketmq.client.apis.message.Message;
import org.apache.rocketmq.client.apis.producer.Producer;
import org.apache.rocketmq.client.apis.producer.SendReceipt;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@Configuration
@EnableConfigurationProperties(OrderTimeoutMqProperties.class)
public class OrderTimeoutMqProducer implements OrderTimeoutDelayPort {

    private final OrderTimeoutMqProperties properties;
    private final ObjectMapper objectMapper;

    private Producer producer;

    public OrderTimeoutMqProducer(OrderTimeoutMqProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void start() {
        if (!properties.isEnabled()) {
            log.info("order-timeout-mq 生产未启用，跳过生产者初始化");
            return;
        }
        try {
            this.producer = buildProducer();
            log.info("order-timeout-mq 生产者启动成功, topic={}, delayMinutes={}",
                    properties.getTopic(), properties.getDelayMinutes());
        } catch (Exception e) {
            throw new IllegalStateException("order-timeout-mq 生产者初始化失败", e);
        }
    }

    private Producer buildProducer() throws ClientException {
        ClientServiceProvider provider = ClientServiceProvider.loadService();

        ClientConfigurationBuilder configBuilder = ClientConfiguration.newBuilder()
                .setEndpoints(properties.getEndpoints())
                .setCredentialProvider(new StaticSessionCredentialsProvider(
                        properties.getAccessKey(), properties.getSecretKey()));
        if (hasText(properties.getNamespace())) {
            configBuilder.setNamespace(properties.getNamespace());
        }

        return provider.newProducerBuilder()
                .setClientConfiguration(configBuilder.build())
                .setTopics(properties.getTopic())
                .build();
    }

    @Override
    public void scheduleTimeout(Order order) {
        if (!properties.isEnabled() || producer == null) {
            log.debug("order-timeout-mq 生产未启用，跳过发送超时延迟消息, orderNo={}", order.getOrderNo());
            return;
        }
        if (order.getOrderNo() == null) {
            return;
        }
        OrderTimeoutMessage message = new OrderTimeoutMessage(order.getOrderNo(), order.getPaymentExpiredAt());
        sendAfterCommit(message);
    }

    private void sendAfterCommit(OrderTimeoutMessage message) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    send(message);
                }
            });
        } else {
            send(message);
        }
    }

    private void send(OrderTimeoutMessage timeoutMessage) {
        String orderNo = timeoutMessage.orderNo();
        try {
            long deliveryTimestamp = System.currentTimeMillis()
                    + Duration.ofMinutes(properties.getDelayMinutes()).toMillis();

            ClientServiceProvider provider = ClientServiceProvider.loadService();
            Message message = provider.newMessageBuilder()
                    .setTopic(properties.getTopic())
                    .setTag("order-timeout")
                    .setKeys(orderNo)
                    .setDeliveryTimestamp(deliveryTimestamp)
                    .setBody(objectMapper.writeValueAsBytes(timeoutMessage))
                    .build();

            SendReceipt receipt = producer.send(message);
            log.info("订单超时延迟消息发送成功, orderNo={}, messageId={}, delayMinutes={}, deliveryAt={}",
                    orderNo, receipt.getMessageId(), properties.getDelayMinutes(),
                    LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(deliveryTimestamp), ZoneId.systemDefault()));
        } catch (Exception e) {
            log.error("订单超时延迟消息发送失败, orderNo={}", orderNo, e);
        }
    }

    @PreDestroy
    public void stop() {
        if (this.producer != null) {
            try {
                this.producer.close();
            } catch (Exception e) {
                log.warn("关闭 order-timeout-mq 生产者失败", e);
            }
        }
        log.info("order-timeout-mq 生产者已关闭");
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
