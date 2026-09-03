package cn.dextea.trade.order.infrastructure.mq;

import cn.dextea.trade.order.application.dto.OrderMakingStatusMessage;
import cn.dextea.trade.order.domain.enumeration.MakingStatus;
import cn.dextea.trade.order.domain.model.Order;
import cn.dextea.trade.order.domain.port.MakingStatusPublisher;
import cn.dextea.trade.order.interfaces.mq.OrderMakingMqProperties;
import cn.dextea.trade.shared.enumeration.CodeEnum;
import cn.dextea.trade.shared.infrastructure.mq.RocketMqClientFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.apis.ClientException;
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

    private static final int UNKNOWN_STATUS_CODE = -1;

    private final OrderMakingMqProperties properties;
    private final RocketMqClientFactory clientFactory;
    private final ObjectMapper objectMapper;

    private Producer producer;

    public OrderMakingMqProducer(OrderMakingMqProperties properties,
                                 RocketMqClientFactory clientFactory,
                                 ObjectMapper objectMapper) {
        this.properties = properties;
        this.clientFactory = clientFactory;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void start() {
        if (!clientFactory.isEnabled()) {
            log.info("rocketmq 总开关未启用，跳过生产者初始化");
            return;
        }
        if (!properties.isActive()) {
            log.info("order-making-mq 未启用，跳过生产者初始化");
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
        return clientFactory.provider().newProducerBuilder()
                .setClientConfiguration(clientFactory.clientConfiguration())
                .setTopics(properties.getTopic())
                .build();
    }

    @Override
    public void publishMakingStatusChange(Order order, MakingStatus fromStatus, MakingStatus toStatus) {
        Long orderId = order.getId();
        if (!clientFactory.isEnabled() || !properties.isActive()) {
            log.debug("order-making-mq 未启用，跳过发送制作状态消息, orderId={}", orderId);
            return;
        }
        if (producer == null) {
            throw new IllegalStateException("order-making-mq 生产者未初始化, 无法发送制作状态消息, orderId=" + orderId);
        }
        sendAfterCommit(buildMessage(order, fromStatus, toStatus));
    }

    private OrderMakingStatusMessage buildMessage(Order order, MakingStatus fromStatus, MakingStatus toStatus) {
        return new OrderMakingStatusMessage(
                order.getId(),
                order.getOrderNo(),
                order.getStoreId(),
                fromStatus.getCode(),
                toStatus.getCode(),
                toCode(order.getDiningMethod()),
                toCode(order.getMakingStatus()),
                toCode(order.getPaymentStatus()),
                order.getPickupCode(),
                order.getTotalPrice().getValue(),
                order.getTotalQuantity().getValue(),
                order.getCreatedAt());
    }

    private int toCode(CodeEnum codeEnum) {
        return codeEnum == null ? UNKNOWN_STATUS_CODE : codeEnum.getCode();
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
        String orderId = String.valueOf(message.orderId());
        try {
            Message rocketMessage = clientFactory.provider().newMessageBuilder()
                    .setTopic(properties.getTopic())
                    .setTag(message.toTag())
                    .setKeys(orderId)
                    .setBody(objectMapper.writeValueAsBytes(message))
                    .build();

            SendReceipt receipt = producer.send(rocketMessage);
            log.info("订单制作状态消息发送成功, orderId={}, messageId={}, tag={}",
                    orderId, receipt.getMessageId(), message.toTag());
        } catch (Exception e) {
            log.error("订单制作状态消息发送失败, orderId={}, tag={}", orderId, message.toTag(), e);
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
}
