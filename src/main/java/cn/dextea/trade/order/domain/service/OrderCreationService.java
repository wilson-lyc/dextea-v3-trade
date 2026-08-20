package cn.dextea.trade.order.domain.service;

import cn.dextea.trade.order.domain.dto.CreateTradeRequest;
import cn.dextea.trade.order.domain.exception.OrderErrorCode;
import cn.dextea.trade.order.domain.model.Customer;
import cn.dextea.trade.order.domain.model.Order;
import cn.dextea.trade.order.domain.model.OrderItem;
import cn.dextea.trade.order.domain.model.Product;
import cn.dextea.trade.order.domain.model.SkuItem;
import cn.dextea.trade.order.domain.enumeration.DiningMethod;
import cn.dextea.trade.order.domain.enumeration.OrderSource;
import cn.dextea.trade.order.domain.port.OrderNoGenerator;
import cn.dextea.trade.order.domain.port.OrderTimeoutDelayPort;
import cn.dextea.trade.order.domain.port.PaymentPort;
import cn.dextea.trade.order.domain.repository.CustomerRepository;
import cn.dextea.trade.order.domain.repository.OrderRepository;
import cn.dextea.trade.order.domain.repository.ProductRepository;
import cn.dextea.trade.order.domain.repository.StoreRepository;
import cn.dextea.trade.shared.enumeration.PaymentMethod;
import cn.dextea.trade.shared.error.BizError;
import cn.dextea.trade.shared.error.CommonErrorCode;
import cn.dextea.trade.shared.model.Money;
import cn.dextea.trade.shared.util.EnsureUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCreationService {

    private final StoreRepository storeRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final SkuIdParser skuIdParser;
    private final OrderItemFactory orderItemFactory;
    private final OrderNoGenerator orderNoGenerator;
    private final PaymentPort paymentPort;
    private final OrderAmountService orderAmountService;
    private final OrderTimeoutDelayPort orderTimeoutDelayPort;

    @Value("${order.payment_ttl:15}")
    private long paymentTtlMinutes;

    public Order preBuildOrder(Long customerId, Long storeId, List<SkuItem> items) {
        EnsureUtil.notNull(customerId, CommonErrorCode.PARAM_MISSING, "请先登录或提供顾客信息");
        EnsureUtil.notNull(storeId, CommonErrorCode.PARAM_MISSING, "请选择门店");
        EnsureUtil.notNull(items, CommonErrorCode.PARAM_MISSING, "请选择商品");
        EnsureUtil.notEmpty(items, CommonErrorCode.PARAM_MISSING, "请至少选择一个商品");
        
        log.info("开始预下单, customerId={}, storeId={}, itemCount={}", customerId, storeId, items.size());

        // 校验门店
        EnsureUtil.notNull(storeRepository.getStoreById(storeId), OrderErrorCode.STORE_NOT_FOUND).ensureActive();

        // 校验顾客
        EnsureUtil.notNull(customerRepository.getCustomerById(customerId), OrderErrorCode.CUSTOMER_NOT_FOUND).ensureActive();

        return buildOrder(customerId, storeId, items);
    }

    public Order createOrder(Long customerId, Long storeId, List<SkuItem> items,
                             OrderSource source, PaymentMethod paymentMethod, DiningMethod diningMethod,
                             String note, String idempotencyKey) {
        EnsureUtil.notNull(customerId, CommonErrorCode.PARAM_MISSING, "请先登录或提供顾客信息");
        EnsureUtil.notNull(storeId, CommonErrorCode.PARAM_MISSING, "请选择门店");
        EnsureUtil.notNull(items, CommonErrorCode.PARAM_MISSING, "请选择商品");
        EnsureUtil.notEmpty(items, CommonErrorCode.PARAM_MISSING, "请至少选择一个商品");
        EnsureUtil.notNull(source, CommonErrorCode.PARAM_MISSING, "请提供订单来源");
        EnsureUtil.notNull(paymentMethod, CommonErrorCode.PARAM_MISSING, "请选择支付方式");
        EnsureUtil.notNull(diningMethod, CommonErrorCode.PARAM_MISSING, "请选择就餐方式");
        EnsureUtil.notNull(idempotencyKey, CommonErrorCode.PARAM_MISSING, "缺少幂等键");
        log.info("开始创建订单, customerId={}, storeId={}, itemCount={}, paymentMethod={}, idempotencyKey={}",
                customerId, storeId, items.size(), paymentMethod, idempotencyKey);

        // 校验门店
        EnsureUtil.notNull(storeRepository.getStoreById(storeId), OrderErrorCode.STORE_NOT_FOUND).ensureActive();

        // 校验顾客
        Customer customer = EnsureUtil.notNull(customerRepository.getCustomerById(customerId),
                OrderErrorCode.CUSTOMER_NOT_FOUND);
        customer.ensureActive();

        // 构建订单
        Order order = buildOrder(customerId, storeId, items);

        // 存在不可售订单项：终止生成订单号与创建交易，降级为预构建结果，仅返回订单数据
        boolean hasUnavailableItem = order.getItems().stream()
                .anyMatch(orderItem -> !Boolean.TRUE.equals(orderItem.getAvailable()));
        if (hasUnavailableItem) {
            long unavailableCount = order.getItems().stream()
                    .filter(orderItem -> !Boolean.TRUE.equals(orderItem.getAvailable()))
                    .count();
            log.warn("创建订单存在不可售商品, 终止下单, customerId={}, storeId={}, itemCount={}, unavailableCount={}",
                    customerId, storeId, order.getItems().size(), unavailableCount);
            return order;
        }

        // 补充订单信息：订单号、来源、支付方式、取餐方式、备注、幂等键
        order.place(orderNoGenerator.next(), source, paymentMethod, diningMethod, note, idempotencyKey);

        // 创建支付单
        LocalDateTime paymentExpiredAt = LocalDateTime.now().plusMinutes(paymentTtlMinutes);
        String tradeNo = paymentPort.createTradeNo(CreateTradeRequest.builder()
                .orderNo(order.getOrderNo())
                .buyerOpenId(resolveBuyerOpenId(customer, paymentMethod))
                .totalPrice(order.getTotalPrice())
                .totalQuantity(order.getTotalQuantity())
                .paymentMethod(paymentMethod)
                .payExpireAt(paymentExpiredAt)
                .build());
        order.markCreated(tradeNo, paymentExpiredAt);
        log.debug("调用支付网关创建交易单成功, customerId={}, orderNo={}, tradeNo={}, payExpiredAt={}",
                customerId, order.getOrderNo(), tradeNo, paymentExpiredAt);

        // 落库
        orderRepository.save(order);
        log.debug("订单落库成功, orderNo={}, orderId={}, idempotencyKey={}",
                order.getOrderNo(), order.getId(), idempotencyKey);

        // 发送支付超时延迟消息
        orderTimeoutDelayPort.scheduleTimeout(order);

        log.info("创建订单成功, customerId={}, storeId={}, orderNo={}, tradeNo={}, totalPrice={}, totalQuantity={}",
                customerId, storeId, order.getOrderNo(), order.getTradeNo(),
                order.getTotalPrice(), order.getTotalQuantity());
        return order;
    }

    private String resolveBuyerOpenId(Customer customer, PaymentMethod method) {
        if (method == PaymentMethod.WEIXIN) {
            return EnsureUtil.notNull(customer.getWeixinOpenId(),
                    OrderErrorCode.INVALID_PAYMENT_METHOD, "微信支付需绑定微信 openId");
        }
        if (method == PaymentMethod.ALIPAY) {
            return EnsureUtil.notNull(customer.getAlipayOpenId(),
                    OrderErrorCode.INVALID_PAYMENT_METHOD, "支付宝支付需绑定支付宝 openId");
        }
        throw new BizError(OrderErrorCode.INVALID_PAYMENT_METHOD, "不支持的支付方式: " + method);
    }

    private Order buildOrder(Long customerId, Long storeId, List<SkuItem> items) {
        // 获取商品
        List<String> skuIds = items.stream()
                .map(SkuItem::getSkuId)
                .collect(Collectors.toList());
        Set<Long> productIds = skuIdParser.extractProductIds(skuIds);
        Map<Long, Product> products = productRepository.getProductByIdsWithStoreId(productIds, storeId);
        log.debug("预下单商品加载完成, customerId={}, storeId={}, skuCount={}, productCount={}",
                customerId, storeId, skuIds.size(), products.size());

        // 创建订单草稿
        Order order = Order.initialize(customerId, storeId);
        log.debug("预下单订单领域对象创建完成, customerId={}, storeId={}", customerId, storeId);

        // 往订单中添加商品
        for (SkuItem item : items) {
            Long productId = skuIdParser.extractProductId(item.getSkuId());
            Product product = products.get(productId);
            if (product == null) {
                log.warn("预下单商品不存在, 列入不可售, customerId={}, storeId={}, skuId={}, productId={}",
                        customerId, storeId, item.getSkuId(), productId);
                order.addItem(OrderItem.create(productId, null, item.getSkuId(), null,
                        null, item.getQuantity(), Money.ZERO, false));
                continue;
            }
            order.addItem(orderItemFactory.create(product, item.getSkuId(), item.getQuantity()));
        }

        // 计算订单金额和数量
        order.assignAmounts(
                orderAmountService.calculateTotalPrice(order),
                orderAmountService.calculateTotalQuantity(order));

        log.info("预下单成功, customerId={}, storeId={}, itemCount={}, totalPrice={}, totalQuantity={}",
                customerId, storeId, order.getItems().size(), order.getTotalPrice(), order.getTotalQuantity());
        return order;
    }
}
