package cn.dextea.trade.order.application.service.impl;

import cn.dextea.trade.common.error.BizError;
import cn.dextea.trade.order.application.command.CreateOrderCommand;
import cn.dextea.trade.order.application.command.OrderProductCommand;
import cn.dextea.trade.order.application.command.PreBuildOrderCommand;
import cn.dextea.trade.order.application.dto.OrderCreateResult;
import cn.dextea.trade.order.application.facade.ExternalDataFacade;
import cn.dextea.trade.order.application.service.OrderApplicationService;
import cn.dextea.trade.order.domain.enums.DiningMethodEnum;
import cn.dextea.trade.order.domain.exception.OrderErrorCode;
import cn.dextea.trade.order.domain.gateway.OrderIdGeneratorGateway;
import cn.dextea.trade.order.domain.gateway.PaymentClientGateway;
import cn.dextea.trade.order.domain.model.valueobject.PreBuildContext;
import cn.dextea.trade.order.domain.model.valueobject.PreBuildProductInput;
import cn.dextea.trade.order.domain.model.valueobject.PreBuildResult;
import cn.dextea.trade.order.domain.model.aggregate.Order;
import cn.dextea.trade.order.domain.model.entity.OrderItem;
import cn.dextea.trade.order.domain.model.valueobject.Customer;
import cn.dextea.trade.order.application.config.OrderPaymentProperties;
import cn.dextea.trade.order.domain.repository.OrderRepository;
import cn.dextea.trade.order.domain.service.OrderPlacementDomainService;
import cn.dextea.trade.pay.domain.exception.PayErrorCode;
import cn.dextea.trade.pay.domain.enums.PlatformEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单应用服务实现（命令侧）：编排幂等、预构建、落库、支付与结果缓存。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderApplicationServiceImpl implements OrderApplicationService {

    private final OrderPlacementDomainService placementDomainService;
    private final OrderRepository orderRepository;
    private final OrderIdGeneratorGateway orderIdGeneratorGateway;
    private final PaymentClientGateway paymentClientGateway;
    private final ExternalDataFacade externalDataFacade;
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
        // 0. 支付方式拦截：微信支付暂未实现
        if (PlatformEnum.WEIXIN.equals(command.getPlatform())) {
            throw new BizError(PayErrorCode.PAY_PLATFORM_NOT_SUPPORTED, "微信支付暂不支持");
        }

        // 校验用餐方式合法性
        DiningMethodEnum diningMethod = DiningMethodEnum.of(command.getDiningMethod());
        if (diningMethod == null) {
            throw new BizError(OrderErrorCode.DINING_METHOD_INVALID, "用餐方式错误: " + command.getDiningMethod());
        }

        String idempotencyKey = command.getIdempotencyKey();
        String redisKey = IDEMPOTENCY_KEY_PREFIX + idempotencyKey;

        // 1. Redis 幂等校验：命中说明是重复请求，直接抛出业务异常
        if (Boolean.TRUE.equals(redisTemplate.hasKey(redisKey))) {
            throw new BizError(OrderErrorCode.ORDER_DUPLICATE_REQUEST, "重复请求，请勿重复下单");
        }

        // 2. 构建订单：校验数据合法性并计价（门店/顾客不可用已在 preBuild 中抛业务异常）
        PreBuildResult summary = placementDomainService.preBuild(toContext(command));

        // 存在不可用项时不创建订单记录，也不占用幂等键，允许修正购物车后正常重试
        if (hasUnavailable(summary)) {
            return OrderCreateResult.builder().preBuild(summary).build();
        }

        // 3. 落库：MySQL 唯一索引兜底，真正保证同幂等键只创建一个订单
        List<OrderItem> items = summary.getProducts().stream()
                .map(p -> OrderItem.builder()
                        .productId(p.getProductId())
                        .skuId(p.getSkuId())
                        .productName(p.getProductName())
                        .coverId(p.getCoverId())
                        .quantity(p.getQuantity())
                        .unitPrice(p.getUnitPrice())
                        .subtotal(p.getSubtotal())
                        .build())
                .toList();

        // 计算订单支付过期时间
        LocalDateTime payExpireAt = LocalDateTime.now().plus(orderPaymentProperties.payTimeout());

        Order order = Order.createInitial(
                orderIdGeneratorGateway.generateOrderNo(),
                idempotencyKey,
                command.getCustomerId(),
                command.getStoreId(),
                command.getPlatform().getCode(),
                diningMethod.getCode(),
                command.getNote(),
                summary.getTotalPrice(),
                summary.getTotalQuantity(),
                payExpireAt,
                items);

        try {
            orderRepository.save(order);
            // 落库成功后标记幂等键，后续携带相同幂等键的请求将被判定为重复请求
            markProcessed(redisKey);
        } catch (DuplicateKeyException e) {
            // Redis 标记过期但 DB 已有记录，同样视为重复请求
            throw new BizError(OrderErrorCode.ORDER_DUPLICATE_REQUEST, "重复请求，请勿重复下单");
        }

        // 4. 支付宝支付：创建交易并回填 trade_no
        if (Integer.valueOf(PlatformEnum.ALIPAY.getCode()).equals(order.getPayMethod()) && order.getTradeNo() == null) {
            Customer customer = externalDataFacade.findCustomer(order.getCustomerId());
            if (customer == null || customer.getAlipayOpenId() == null) {
                throw new BizError(PayErrorCode.ALIPAY_BUYER_NOT_BOUND, "顾客未绑定支付宝，无法创建支付");
            }
            String tradeNo = paymentClientGateway.createPayment(
                    order.getOrderNo(), order.getTotalPrice(), customer.getAlipayOpenId(),
                    order.getTotalQuantity(), order.getPayMethod(), order.getPayExpireAt());
            order.markTradeNo(tradeNo);
            orderRepository.updateTradeNo(order.getId(), tradeNo);
        }

        // 5. 组装创建结果返回
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

    private static boolean hasUnavailable(PreBuildResult summary) {
        boolean products = summary.getUnavailableProducts() != null && !summary.getUnavailableProducts().isEmpty();
        boolean customization = summary.getUnavailableCustomizations() != null && !summary.getUnavailableCustomizations().isEmpty();
        return products || customization;
    }

    private void markProcessed(String redisKey) {
        try {
            redisTemplate.opsForValue().set(redisKey, "1", IDEMPOTENCY_TTL);
        } catch (RuntimeException e) {
            log.warn("Redis 标记幂等键失败（不影响下单）: {}", e.getMessage());
        }
    }
}
