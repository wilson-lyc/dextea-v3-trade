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
import cn.dextea.trade.order.domain.model.PreBuildContext;
import cn.dextea.trade.order.domain.model.PreBuildProductInput;
import cn.dextea.trade.order.domain.model.PreBuildResult;
import cn.dextea.trade.order.domain.model.aggregate.Order;
import cn.dextea.trade.order.domain.model.entity.OrderItem;
import cn.dextea.trade.order.domain.model.valueobject.Customer;
import cn.dextea.trade.order.application.config.OrderPaymentProperties;
import cn.dextea.trade.order.domain.repository.OrderRepository;
import cn.dextea.trade.order.domain.service.OrderPlacementDomainService;
import cn.dextea.trade.pay.domain.exception.PayErrorCode;
import cn.dextea.trade.pay.domain.enums.PlatformEnum;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
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
    private final ObjectMapper objectMapper;
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

        // 1. Redis 快校验：命中说明已创建过，直接返回结果
        OrderCreateResult cached = getCachedResult(redisKey);
        if (cached != null) {
            return cached;
        }

        // 2. 构建订单：校验数据合法性并计价
        PreBuildResult summary = placementDomainService.preBuild(toContext(command));

        // 门店或顾客不可用时直接拒绝下单
        if (!Boolean.TRUE.equals(summary.isStoreAvailable())) {
            throw new BizError(OrderErrorCode.STORE_NOT_OPEN, "门店不可下单: " + command.getStoreId());
        }
        if (!Boolean.TRUE.equals(summary.isCustomerAvailable())) {
            throw new BizError(OrderErrorCode.CUSTOMER_NOT_ACTIVE, "顾客不可下单: " + command.getCustomerId());
        }

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

        // 支付过期时间点由本系统计算（当前时间 + 配置的超时时长），落库并同步给支付宝，
        // 保证系统与支付宝两端关单时刻一致，前端可据此做支付倒计时
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

        boolean newlyCreated = true;
        try {
            orderRepository.save(order);
        } catch (DuplicateKeyException e) {
            // Redis 快校验过期但 DB 已有记录：查回已存在订单，复用其订单号与交易号
            Order existing = orderRepository.findByIdempotencyKey(idempotencyKey);
            if (existing == null) {
                throw new BizError(OrderErrorCode.ORDER_CREATE_FAILED, "订单创建冲突，请稍后重试");
            }
            order = existing;
            newlyCreated = false;
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

        // 5. 缓存首次结果，后续携带相同幂等键的请求直接返回，无需再查 DB
        OrderCreateResult result = OrderCreateResult.builder()
                .id(order.getId())
                .orderNo(order.getOrderNo())
                .tradeNo(order.getTradeNo())
                .payExpireAt(order.getPayExpireAt())
                .preBuild(summary)
                .build();
        if (newlyCreated) {
            cacheResult(redisKey, result);
        }
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

    private OrderCreateResult getCachedResult(String redisKey) {
        try {
            String json = redisTemplate.opsForValue().get(redisKey);
            if (json == null) {
                return null;
            }
            return objectMapper.readValue(json, OrderCreateResult.class);
        } catch (RuntimeException | IOException e) {
            log.warn("Redis 读取幂等结果失败，降级至 MySQL 唯一索引: {}", e.getMessage());
            return null;
        }
    }

    private void cacheResult(String redisKey, OrderCreateResult result) {
        try {
            String json = objectMapper.writeValueAsString(result);
            redisTemplate.opsForValue().set(redisKey, json, IDEMPOTENCY_TTL);
        } catch (RuntimeException | IOException e) {
            log.warn("Redis 缓存幂等结果失败（不影响下单）: {}", e.getMessage());
        }
    }
}
