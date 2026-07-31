package cn.dextea.trade.order.application.service.impl;
import cn.dextea.trade.shared.domain.error.BizError;
import cn.dextea.trade.order.application.command.CreateOrderCommand;
import cn.dextea.trade.order.application.command.OrderProductCommand;
import cn.dextea.trade.order.application.command.PreBuildOrderCommand;
import cn.dextea.trade.order.application.dto.OrderCreateResult;
import cn.dextea.trade.order.application.service.OrderApplicationService;
import cn.dextea.trade.order.domain.exception.OrderErrorCode;
import cn.dextea.trade.order.domain.gateway.OrderIdGeneratorGateway;
import cn.dextea.trade.order.domain.model.aggregate.Order;
import cn.dextea.trade.order.domain.model.valueobject.PreBuildContext;
import cn.dextea.trade.order.domain.model.valueobject.PreBuildProductInput;
import cn.dextea.trade.order.domain.model.valueobject.PreBuildResult;
import cn.dextea.trade.order.application.config.OrderPaymentProperties;
import cn.dextea.trade.order.domain.repository.OrderRepository;
import cn.dextea.trade.order.domain.service.OrderPlacementDomainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.util.List;
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderApplicationServiceImpl implements OrderApplicationService {
    private final OrderPlacementDomainService placementDomainService;
    private final OrderRepository orderRepository;
    private final OrderIdGeneratorGateway orderIdGeneratorGateway;
    private final StringRedisTemplate redisTemplate;
    private final OrderPaymentProperties orderPaymentProperties;
    private static final String IDEMPOTENCY_KEY_PREFIX = "dextea:order:idem:";
    private static final Duration IDEMPOTENCY_TTL = Duration.ofHours(24);
    @Override
    public PreBuildResult preBuildOrder(PreBuildOrderCommand command) {
        return placementDomainService.preBuild(toContext(command));
    }
    @Override
    public OrderCreateResult createOrder(CreateOrderCommand command) {
        String idempotencyKey = command.getIdempotencyKey();
        String redisKey = IDEMPOTENCY_KEY_PREFIX + idempotencyKey;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(redisKey))) {
            throw new BizError(OrderErrorCode.ORDER_DUPLICATE_REQUEST, "重复请求，请勿重复下单");
        }
        placementDomainService.validatePlacement(command.getPlatform().getCode(), command.getDiningMethod());
        PreBuildResult summary = placementDomainService.preBuild(toContext(command));
        if (summary.hasUnavailable()) {
            return OrderCreateResult.builder().preBuild(summary).build();
        }
        Order order = Order.createFromPreBuild(
                orderIdGeneratorGateway.generateOrderNo(),
                idempotencyKey,
                command.getCustomerId(),
                command.getStoreId(),
                command.getPlatform().getCode(),
                command.getDiningMethod(),
                command.getNote(),
                orderPaymentProperties.payTimeout(),
                summary);
        placementDomainService.initiatePayment(order);
        try {
            orderRepository.save(order);
            markProcessed(redisKey);
        } catch (DuplicateKeyException e) {
            throw new BizError(OrderErrorCode.ORDER_DUPLICATE_REQUEST, "重复请求，请勿重复下单");
        }
        OrderCreateResult result = OrderCreateResult.builder()
                .id(order.getId())
                .orderNo(order.getOrderNo())
                .tradeNo(order.getTradeNo())
                .payExpireAt(order.getPayExpireAt())
                .preBuild(summary)
                .build();
        return result;
    }
    private PreBuildContext toContext(CreateOrderCommand command) {
        return toContext(command.getStoreId(), command.getCustomerId(), command.getProducts());
    }
    private PreBuildContext toContext(PreBuildOrderCommand command) {
        return toContext(command.getStoreId(), command.getCustomerId(), command.getProducts());
    }
    private PreBuildContext toContext(Long storeId, Long customerId, List<OrderProductCommand> products) {
        List<PreBuildProductInput> inputs = products.stream()
                .map(p -> PreBuildProductInput.builder()
                        .skuId(p.getSkuId())
                        .quantity(p.getQuantity())
                        .build())
                .toList();
        return PreBuildContext.builder()
                .storeId(storeId)
                .customerId(customerId)
                .products(inputs)
                .build();
    }
    private void markProcessed(String redisKey) {
        try {
            redisTemplate.opsForValue().set(redisKey, "1", IDEMPOTENCY_TTL);
        } catch (RuntimeException e) {
            log.warn("Redis 标记幂等键失败（不影响下单）: {}", e.getMessage());
        }
    }
}
