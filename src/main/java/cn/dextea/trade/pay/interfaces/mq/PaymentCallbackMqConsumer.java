//package cn.dextea.trade.pay.interfaces.mq;
//
//import jakarta.annotation.PostConstruct;
//import jakarta.annotation.PreDestroy;
//import java.time.Duration;
//import java.util.List;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.rocketmq.client.apis.ClientConfiguration;
//import org.apache.rocketmq.client.apis.ClientException;
//import org.apache.rocketmq.client.apis.ClientServiceProvider;
//import org.apache.rocketmq.client.apis.StaticSessionCredentialsProvider;
//import org.apache.rocketmq.client.apis.consumer.FilterExpression;
//import org.apache.rocketmq.client.apis.consumer.FilterExpressionType;
//import org.apache.rocketmq.client.apis.consumer.SimpleConsumer;
//import org.apache.rocketmq.client.apis.message.MessageView;
//import org.springframework.boot.context.properties.EnableConfigurationProperties;
//import org.springframework.context.annotation.Configuration;
//
//@Slf4j
//@Configuration
//@EnableConfigurationProperties(PaymentCallbackMqProperties.class)
//public class PaymentCallbackMqConsumer {
//
//    private static final int MAX_RECEIVE_NUM = 16;
//
//    private static final Duration RECEIVE_TIMEOUT = Duration.ofSeconds(20);
//
//    private final PaymentCallbackMqProperties properties;
//
//    private SimpleConsumer consumer;
//    private ExecutorService consumeExecutor;
//    private volatile boolean running = false;
//
//    public PaymentCallbackMqConsumer(PaymentCallbackMqProperties properties) {
//        this.properties = properties;
//    }
//
//    @PostConstruct
//    public void start() {
//        if (!properties.isEnabled()) {
//            log.info("payment-callback-mq 消费未启用，跳过消费者初始化");
//            return;
//        }
//        try {
//            this.consumer = buildConsumer();
//            this.running = true;
//            this.consumeExecutor = Executors.newSingleThreadExecutor(r -> new Thread(r, "payment-callback-mq-consumer"));
//            this.consumeExecutor.submit(this::consumeLoop);
//            log.info("payment-callback-mq 消费者启动成功, topic={}, consumerGroup={}, tag={}",
//                    properties.getTopic(), properties.getConsumerGroup(), properties.getTag());
//        } catch (Exception e) {
//            throw new IllegalStateException("payment-callback-mq 消费者初始化失败", e);
//        }
//    }
//
//    private SimpleConsumer buildConsumer() throws ClientException {
//        ClientServiceProvider provider = ClientServiceProvider.loadService();
//
//        ClientConfiguration.Builder configBuilder = ClientConfiguration.newBuilder()
//                .setEndpoints(properties.getEndpoints())
//                .setCredentialProvider(new StaticSessionCredentialsProvider(
//                        properties.getAccessKey(), properties.getSecretKey()));
//        if (hasText(properties.getNamespace())) {
//            configBuilder.setNamespace(properties.getNamespace());
//        }
//
//        FilterExpression filterExpression = new FilterExpression(properties.getTag(), FilterExpressionType.TAG);
//        return provider.newSimpleConsumerBuilder()
//                .setClientConfiguration(configBuilder.build())
//                .setConsumerGroup(properties.getConsumerGroup())
//                .setFilterExpression(properties.getTopic(), filterExpression)
//                .setAwaitDuration(RECEIVE_TIMEOUT)
//                .build();
//    }
//
//    private void consumeLoop() {
//        while (running) {
//            try {
//                List<MessageView> messages = consumer.receive(MAX_RECEIVE_NUM, RECEIVE_TIMEOUT);
//                for (MessageView message : messages) {
//                    try {
//                        handleMessage(message);
//                        consumer.ack(message);
//                    } catch (Exception e) {
//                        log.error("payment-callback 消息处理失败, messageId={}", message.getMessageId(), e);
//                    }
//                }
//            } catch (Exception e) {
//                log.error("payment-callback 消息拉取异常", e);
//                sleepQuietly(Duration.ofSeconds(1));
//            }
//        }
//    }
//
//    private void handleMessage(MessageView message) throws Exception {
//        log.info("收到支付回调消息, messageId={}, topic={}, body={}",
//                message.getMessageId(), message.getTopic(), new String(message.getBody()));
//    }
//
//    @PreDestroy
//    public void stop() {
//        this.running = false;
//        if (this.consumeExecutor != null) {
//            this.consumeExecutor.shutdown();
//        }
//        if (this.consumer != null) {
//            try {
//                this.consumer.close();
//            } catch (Exception e) {
//                log.warn("关闭 payment-callback-mq 消费者失败", e);
//            }
//        }
//        log.info("payment-callback-mq 消费者已关闭");
//    }
//
//    private boolean hasText(String value) {
//        return value != null && !value.isBlank();
//    }
//
//    private void sleepQuietly(Duration duration) {
//        try {
//            Thread.sleep(duration.toMillis());
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//        }
//    }
//}
