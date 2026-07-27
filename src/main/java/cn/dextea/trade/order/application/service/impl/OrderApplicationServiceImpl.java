package cn.dextea.trade.order.application.service.impl;

import cn.dextea.trade.common.error.BizError;
import cn.dextea.trade.order.application.command.CreateOrderCommand;
import cn.dextea.trade.order.application.command.OrderProductCommand;
import cn.dextea.trade.order.application.command.PreBuildOrderCommand;
import cn.dextea.trade.order.application.dto.OrderCreateResult;
import cn.dextea.trade.order.application.service.OrderApplicationService;
import cn.dextea.trade.order.domain.exception.OrderErrorCode;
import cn.dextea.trade.order.domain.gateway.OrderIdGeneratorGateway;
import cn.dextea.trade.order.domain.model.valueobject.PreBuildContext;
import cn.dextea.trade.order.domain.model.valueobject.PreBuildProductInput;
import cn.dextea.trade.order.domain.model.valueobject.PreBuildResult;
import cn.dextea.trade.order.domain.model.aggregate.Order;
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

        // 0. Redis 幂等校验：命中说明是重复请求，直接抛出业务异常
        if (Boolean.TRUE.equals(redisTemplate.hasKey(redisKey))) {
            throw new BizError(OrderErrorCode.ORDER_DUPLICATE_REQUEST, "重复请求，请勿重复下单");
        }

        // 1. 领域规则校验：支付平台可用性 + 用餐方式合法性（不变式由领域层守卫）
        //    应用层在此将 pay 域的 PlatformEnum 翻译为整型契约，order 域保持自封闭
        placementDomainService.validatePlacement(command.getPlatform().getCode(), command.getDiningMethod());

        // 2. 预构建：校验数据合法性并计价（门店/顾客不可用已在 preBuild 中抛业务异常）
        PreBuildResult summary = placementDomainService.preBuild(toContext(command));

        // 存在不可用项时不创建订单记录，也不占用幂等键，允许修正购物车后正常重试
        if (summary.hasUnavailable()) {
            return OrderCreateResult.builder().preBuild(summary).build();
        }

        // 3. 装配聚合（领域工厂）：明细映射、支付过期时间推导、创建期不变式均收敛于领域
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

        // 4. 先发起支付：平台决策 + 绑卡校验 + 调用支付网关，由领域服务封装并回填 trade_no。
        //    若支付创建失败，订单尚未落库、幂等键也未标记，用户可携相同幂等键安全重试（不会卡死）。
        //    initiatePayment 仅依赖聚合内已有字段（orderNo/totalPrice 等），不依赖 DB 自增 id。
        placementDomainService.initiatePayment(order);

        // 5. 再落库：MySQL 唯一索引兜底，真正保证同幂等键只创建一个订单。
        //    trade_no 已在第 4 步写入聚合，insert SQL 一并持久化，无需二次 updateTradeNo。
        try {
            orderRepository.save(order);
            // 落库成功后标记幂等键，后续携带相同幂等键的请求将被判定为重复请求
            markProcessed(redisKey);
        } catch (DuplicateKeyException e) {
            // Redis 标记过期但 DB 已有记录，同样视为重复请求
            throw new BizError(OrderErrorCode.ORDER_DUPLICATE_REQUEST, "重复请求，请勿重复下单");
        }

        // 6. 组装创建结果返回
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
