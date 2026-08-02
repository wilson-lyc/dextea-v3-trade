package cn.dextea.trade.order.application.service.impl;

import cn.dextea.trade.order.application.dto.command.CreateOrderCommand;
import cn.dextea.trade.order.application.dto.command.PreBuildOrderCommand;
import cn.dextea.trade.order.application.config.OrderPaymentProperties;
import cn.dextea.trade.order.application.dto.result.OrderCreateResult;
import cn.dextea.trade.order.application.dto.result.PreBuildOrderResult;
import cn.dextea.trade.order.application.loader.CatalogSnapshotLoader;
import cn.dextea.trade.order.application.service.OrderApplicationService;
import cn.dextea.trade.order.domain.exception.OrderErrorCode;
import cn.dextea.trade.order.domain.factory.OrderNumberFactory;
import cn.dextea.trade.order.domain.model.aggregate.Order;
import cn.dextea.trade.order.domain.model.valueobject.CatalogSnapshot;
import cn.dextea.trade.order.domain.model.valueobject.PaymentMethod;
import cn.dextea.trade.order.domain.model.valueobject.PreBuildProductInput;
import cn.dextea.trade.order.domain.model.valueobject.SkuSelection;
import cn.dextea.trade.order.domain.port.PaymentClient;
import cn.dextea.trade.order.domain.repository.OrderRepository;
import cn.dextea.trade.order.domain.service.OrderPlacementDomainService;
import cn.dextea.trade.shared.domain.error.BizError;
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

    private static final String IDEMPOTENCY_KEY_PREFIX = "dextea:order:idem:";
    private static final Duration IDEMPOTENCY_TTL = Duration.ofHours(24);

    private final OrderPlacementDomainService placementDomainService;
    private final CatalogSnapshotLoader catalogSnapshotLoader;
    private final OrderRepository orderRepository;
    private final OrderNumberFactory orderNumberFactory;
    private final PaymentClient paymentClient;
    private final StringRedisTemplate redisTemplate;
    private final OrderPaymentProperties orderPaymentProperties;

    @Override
    public PreBuildOrderResult preBuildOrder(PreBuildOrderCommand command) {
        return preBuild(command.getStoreId(), command.getCustomerId(), command.getItems()).result();
    }

    @Override
    public OrderCreateResult createOrder(CreateOrderCommand command) {
        String redisKey = IDEMPOTENCY_KEY_PREFIX + command.getIdempotencyKey();
        if (Boolean.TRUE.equals(redisTemplate.hasKey(redisKey))) {
            throw new BizError(OrderErrorCode.ORDER_DUPLICATE_REQUEST, "重复请求，请勿重复下单");
        }

        int platformCode = command.getPlatform().getCode();
        placementDomainService.validatePlacement(platformCode, command.getDiningMethod());

        PreBuild preBuild = preBuild(command.getStoreId(), command.getCustomerId(), command.getItems());
        PreBuildResult summary = preBuild.result();
        if (summary.hasUnavailable()) {
            return OrderCreateResult.builder().preBuild(summary).build();
        }

        Order order = Order.createFromPreBuild(
                orderNumberFactory.create(),
                command.getIdempotencyKey(),
                command.getCustomerId(),
                command.getStoreId(),
                PaymentMethod.of(platformCode),
                command.getDiningMethod(),
                command.getNote(),
                orderPaymentProperties.payTimeout(),
                summary);

        initiatePayment(order, preBuild.snapshot(), platformCode);

        try {
            orderRepository.save(order);
            markProcessed(redisKey);
        } catch (DuplicateKeyException e) {
            throw new BizError(OrderErrorCode.ORDER_DUPLICATE_REQUEST, "重复请求，请勿重复下单");
        }

        return OrderCreateResult.builder()
                .id(order.getId())
                .orderNo(order.getOrderNo().getValue())
                .tradeNo(order.getTradeNo())
                .payExpireAt(order.getPaymentExpiredAt())
                .preBuild(summary)
                .build();
    }

    private void initiatePayment(Order order, CatalogSnapshot snapshot, int platformCode) {
        if (!placementDomainService.needsImmediatePayment(platformCode)) {
            return;
        }
        String buyerOpenId = placementDomainService.resolveBuyerOpenId(snapshot.getCustomer());
        String tradeNo = paymentClient.createPayment(
                order.getOrderNo().getValue(),
                order.getTotalPrice().getValue(),
                buyerOpenId,
                order.getTotalQuantity().getValue(),
                platformCode,
                order.getPaymentExpiredAt());
        order.markTradeNo(tradeNo);
    }

    private PreBuild preBuild(Long storeId, Long customerId, List<? extends cn.dextea.trade.order.application.dto.shared.AbstractOrderItem> products) {
        List<PreBuildProductInput> inputs = products.stream()
                .map(p -> PreBuildProductInput.builder()
                        .skuId(p.getSkuId())
                        .quantity(p.getQuantity())
                        .build())
                .toList();
        List<SkuSelection> selections = SkuSelection.parseAll(inputs);
        CatalogSnapshot snapshot = catalogSnapshotLoader.load(storeId, customerId, selections);
        return new PreBuild(placementDomainService.preBuild(selections, snapshot), snapshot);
    }

    private void markProcessed(String redisKey) {
        try {
            redisTemplate.opsForValue().set(redisKey, "1", IDEMPOTENCY_TTL);
        } catch (RuntimeException e) {
            log.warn("Redis 标记幂等键失败（不影响下单）: {}", e.getMessage());
        }
    }

    private record PreBuild(PreBuildResult result, CatalogSnapshot snapshot) {
    }
}
