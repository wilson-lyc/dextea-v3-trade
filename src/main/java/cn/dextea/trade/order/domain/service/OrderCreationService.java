package cn.dextea.trade.order.domain.service;

import cn.dextea.trade.order.domain.dto.CreateTradeRequest;
import cn.dextea.trade.order.domain.exception.OrderErrorCode;
import cn.dextea.trade.order.domain.model.Customer;
import cn.dextea.trade.order.domain.model.Order;
import cn.dextea.trade.order.domain.model.Product;
import cn.dextea.trade.order.domain.model.SkuItem;
import cn.dextea.trade.order.domain.enumeration.DiningMethod;
import cn.dextea.trade.order.domain.enumeration.OrderSource;
import cn.dextea.trade.order.domain.port.OrderNoGenerator;
import cn.dextea.trade.order.domain.port.PaymentPort;
import cn.dextea.trade.order.domain.repository.CustomerRepository;
import cn.dextea.trade.order.domain.repository.OrderRepository;
import cn.dextea.trade.order.domain.repository.ProductRepository;
import cn.dextea.trade.order.domain.repository.StoreRepository;
import cn.dextea.trade.shared.enumeration.PaymentMethod;
import cn.dextea.trade.shared.error.BizError;
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

    @Value("${order.payment_ttl:15}")
    private long paymentTtlMinutes;

    public Order preBuildOrder(Long customerId, Long storeId, List<SkuItem> items) {
        log.info("开始预下单, customerId={}, storeId={}, itemCount={}", customerId, storeId, items.size());

        EnsureUtil.notNull(storeRepository.getStoreById(storeId), OrderErrorCode.STORE_NOT_FOUND).ensureActive();

        EnsureUtil.notNull(customerRepository.getCustomerById(customerId), OrderErrorCode.CUSTOMER_NOT_FOUND).ensureActive();

        List<String> skuIds = items.stream()
                .map(SkuItem::getSkuId)
                .collect(Collectors.toList());
        Set<Long> productIds = skuIdParser.extractProductIds(skuIds);
        Map<Long, Product> products = productRepository.getProductByIdsWithStoreId(productIds, storeId);
        log.debug("预下单商品加载完成, customerId={}, storeId={}, skuCount={}, productCount={}",
                customerId, storeId, skuIds.size(), products.size());

        Order order = Order.createDraft(customerId, storeId);
        log.debug("预下单订单领域对象创建完成, customerId={}, storeId={}", customerId, storeId);

        for (SkuItem item : items) {
            Long productId = skuIdParser.extractProductId(item.getSkuId());
            Product product = products.get(productId);
            if (product == null) {
                log.warn("预下单失败, 商品不存在, customerId={}, storeId={}, skuId={}, productId={}",
                        customerId, storeId, item.getSkuId(), productId);
                throw new BizError(OrderErrorCode.PRODUCT_NOT_FOUND, "商品不存在: productId=" + productId);
            }
            order.addItem(orderItemFactory.create(product, item.getSkuId(), item.getQuantity()));
        }

        order.assignAmounts(
                orderAmountService.calculateTotalPrice(order),
                orderAmountService.calculateTotalQuantity(order));

        log.info("预下单成功, customerId={}, storeId={}, itemCount={}, totalPrice={}, totalQuantity={}",
                customerId, storeId, order.getItems().size(), order.getTotalPrice(), order.getTotalQuantity());
        return order;
    }

    public Order createOrder(Long customerId, Long storeId, List<SkuItem> items,
                             OrderSource source, PaymentMethod paymentMethod, DiningMethod diningMethod,
                             String note, String idempotencyKey) {
        log.info("开始创建订单, customerId={}, storeId={}, itemCount={}, paymentMethod={}, idempotencyKey={}",
                customerId, storeId, items.size(), paymentMethod, idempotencyKey);

        // 复用 preBuildOrder 完成门店/顾客校验、商品加载与订单明细初始化
        Order order = preBuildOrder(customerId, storeId, items);

        // 存在不可售订单项：终止生成订单号与创建交易，降级为预构建结果，仅返回订单数据
        boolean hasUnavailableItem = order.getItems().stream()
                .anyMatch(orderItem -> !Boolean.TRUE.equals(orderItem.getAvailable()));
        if (hasUnavailableItem) {
            log.warn("创建订单存在不可售商品, 终止下单, customerId={}, storeId={}, itemCount={}",
                    customerId, storeId, items.size());
            return order;
        }

        // 补充订单信息：订单号、来源、支付方式、取餐方式、备注、幂等键
        order.place(orderNoGenerator.next(), source, paymentMethod, diningMethod, note, idempotencyKey,
                order.getTotalPrice(), order.getTotalQuantity());

        // 创建支付单
        Customer customer = customerRepository.getCustomerById(customerId);
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
        order.save(orderRepository);
        log.debug("订单落库成功, orderNo={}, orderId={}, idempotencyKey={}",
                order.getOrderNo(), order.getId(), idempotencyKey);

        log.info("创建订单成功, customerId={}, storeId={}, orderNo={}, tradeNo={}, totalPrice={}, totalQuantity={}",
                customerId, storeId, order.getOrderNo(), order.getTradeNo(),
                order.getTotalPrice(), order.getTotalQuantity());
        return order;
    }

    private String resolveBuyerOpenId(Customer customer, PaymentMethod method) {
        if (method == PaymentMethod.WEIXIN) {
            return customer.getWeixinOpenId();
        }
        if (method == PaymentMethod.ALIPAY) {
            return customer.getAlipayOpenId();
        }
        return null;
    }
}
