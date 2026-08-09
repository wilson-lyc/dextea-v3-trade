package cn.dextea.trade.order.infrastructure.mq;

import cn.dextea.trade.order.application.dto.OrderMakingStatusMessage;
import cn.dextea.trade.order.domain.enumeration.MakingStatus;
import cn.dextea.trade.order.domain.port.MakingStatusPublisher;
import cn.dextea.trade.order.interfaces.mq.OrderMakingMqProperties;
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

@Slf4j
@Configuration
@EnableConfigurationProperties(OrderMakingMqProperties.class)
public class OrderMakingMqProducer implements MakingStatusPublisher {

    private final OrderMakingMqProperties properties;
    private final ObjectMapper objectMapper;

    private Producer producer;

    public OrderMakingMqProducer(OrderMakingMqProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void start() {
        if (!properties.isProducerActive()) {
            log.info("order-making-mq 生产未启用，跳过生产者初始化");
            return;
        }
        try {
            this.producer = buildProducer();
            log.info("order-making-mq 生产者启动成功, topic={}", properties.getTopic());
        } catch (Exception e) {
            throw new IllegalStateException("order-making-mq 生产者初始化失败", e);
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

    public void publishMakingStatusChange(String orderNo, MakingStatus fromStatus, MakingStatus toStatus) {
        if (!properties.isProducerActive()) {
            log.debug("order-making-mq 生产未启用，跳过发送制作状态消息, orderNo={}", orderNo);
            return;
        }
        if (producer == null) {
            throw new IllegalStateException("order-making-mq 生产者未初始化, 无法发送制作状态消息, orderNo=" + orderNo);
        }
        OrderMakingStatusMessage message = new OrderMakingStatusMessage(orderNo, fromStatus, toStatus);
        sendAfterCommit(message);
    }

    private void sendAfterCommit(OrderMakingStatusMessage message) {
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

    private void send(OrderMakingStatusMessage message) {
        String orderNo = message.orderNo();
        try {
            ClientServiceProvider provider = ClientServiceProvider.loadService();
            Message rocketMessage = provider.newMessageBuilder()
                    .setTopic(properties.getTopic())
                    .setTag(message.toTag())
                    .setKeys(orderNo)
                    .setBody(objectMapper.writeValueAsBytes(message))
                    .build();

            SendReceipt receipt = producer.send(rocketMessage);
            log.info("订单制作状态消息发送成功, orderNo={}, messageId={}, tag={}",
                    orderNo, receipt.getMessageId(), message.toTag());
        } catch (Exception e) {
            log.error("订单制作状态消息发送失败, orderNo={}, tag={}", orderNo, message.toTag(), e);
        }
    }

    @PreDestroy
    public void stop() {
        if (this.producer != null) {
            try {
                this.producer.close();
            } catch (Exception e) {
                log.warn("关闭 order-making-mq 生产者失败", e);
            }
        }
        log.info("order-making-mq 生产者已关闭");
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
