package cn.dextea.trade.order.application.usecase;

import cn.dextea.trade.order.application.assembler.OrderItemAssembler;
import cn.dextea.trade.order.application.dto.command.CreateOrderCommand;
import cn.dextea.trade.order.application.dto.result.OrderCreateResult;
import cn.dextea.trade.order.application.dto.shared.CreateOrderItem;
import cn.dextea.trade.order.domain.dto.CreateTradeRequest;
import cn.dextea.trade.order.domain.exception.OrderErrorCode;
import cn.dextea.trade.order.domain.model.Customer;
import cn.dextea.trade.order.domain.model.Order;
import cn.dextea.trade.order.domain.model.OrderItem;
import cn.dextea.trade.order.domain.model.Product;
import cn.dextea.trade.order.domain.model.Store;
import cn.dextea.trade.order.domain.port.IdempotencyStore;
import cn.dextea.trade.order.domain.port.OrderCreateLock;
import cn.dextea.trade.order.domain.port.OrderNoGenerator;
import cn.dextea.trade.order.domain.port.PaymentPort;
import cn.dextea.trade.order.domain.repository.CustomerRepository;
import cn.dextea.trade.order.domain.repository.OrderRepository;
import cn.dextea.trade.order.domain.repository.ProductRepository;
import cn.dextea.trade.order.domain.repository.StoreRepository;
import cn.dextea.trade.order.domain.service.SkuIdService;
import cn.dextea.trade.shared.domain.enumeration.PaymentMethod;
import cn.dextea.trade.shared.domain.error.BizError;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateOrderUseCase {

    private static final int PAY_EXPIRE_MINUTES = 15;

    private final StoreRepository storeRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final SkuIdService skuIdService;
    private final OrderNoGenerator orderNoGenerator;
    private final PaymentPort paymentPort;
    private final IdempotencyStore idempotencyStore;
    private final OrderCreateLock orderCreateLock;

    @Value("${order.create_order_lock_ttl:1}")
    private long createOrderLockTtlMinutes;

    public OrderCreateResult execute(CreateOrderCommand command) {
        String idempotencyKey = command.getIdempotencyKey();
        Long customerId = command.getCustomerId();
        Long storeId = command.getStoreId();
        log.info("收到创建订单请求, customerId={}, storeId={}, idempotencyKey={}, paymentMethod={}, items={}",
                customerId, storeId, idempotencyKey, command.getPaymentMethod(), command.getItems().size());

        // 第一次校验幂等键，快速失效重复请求
        if (idempotencyStore.exists(idempotencyKey)) {
            log.warn("创建订单幂等键已存在(首次校验), 拒绝重复请求, customerId={}, idempotencyKey={}",
                    customerId, idempotencyKey);
            throw new BizError(OrderErrorCode.IDEMPOTENCY_KEY_CONFLICT);
        }

        // 加锁
        String lockKey = "customer_" + customerId;
        String lockToken = UUID.randomUUID().toString();
        if (!orderCreateLock.tryLock(lockKey, lockToken, Duration.ofMinutes(createOrderLockTtlMinutes))) {
            log.warn("获取创建订单分布式锁失败, 已有订单正在处理中, customerId={}, lockKey={}, lockTtlMinutes={}",
                    customerId, lockKey, createOrderLockTtlMinutes);
            throw new BizError(OrderErrorCode.ORDER_CREATE_IN_PROGRESS);
        }
        log.debug("获取创建订单分布式锁成功, customerId={}, lockKey={}", customerId, lockKey);

        try {
            // 二次校验幂等键，防止并发穿透
            if (idempotencyStore.exists(idempotencyKey)) {
                log.warn("创建订单幂等键已存在(二次校验), 拒绝重复请求, customerId={}, idempotencyKey={}",
                        customerId, idempotencyKey);
                throw new BizError(OrderErrorCode.IDEMPOTENCY_KEY_CONFLICT);
            }
            return doCreate(command, idempotencyKey);
        } finally {
            // 释放锁
            try {
                orderCreateLock.unlock(lockKey, lockToken);
                log.debug("释放创建订单分布式锁成功, customerId={}, lockKey={}", customerId, lockKey);
            } catch (Exception e) {
                log.error("释放创建订单锁失败, customerId={}, lockKey={}", customerId, lockKey, e);
            }
        }
    }

    private OrderCreateResult doCreate(CreateOrderCommand command, String idempotencyKey) {
        Store store = storeRepository.getStoreById(command.getStoreId());
        store.ensureActive();

        Customer customer = customerRepository.getCustomerById(command.getCustomerId());
        customer.ensureActive();

        List<String> skuIds = command.getItems().stream()
                .map(item -> item.getSkuId())
                .collect(Collectors.toList());
        Set<Long> productIds = skuIdService.extractProductIds(skuIds);
        Map<Long, Product> products = productRepository.getProductByIdsWithStoreId(productIds, store.getId());

        Order order = Order.create(command.getCustomerId(), command.getStoreId(), orderNoGenerator,
                command.getSource(), command.getPaymentMethod(), command.getDiningMethod(),
                command.getNote(), idempotencyKey, orderRepository);
        log.debug("创建订单领域对象完成, customerId={}, storeId={}, orderNo={}, idempotencyKey={}",
                command.getCustomerId(), command.getStoreId(), order.getOrderNo(), idempotencyKey);

        List<CreateOrderItem> availableItems = new ArrayList<>();
        List<CreateOrderItem> unavailableItems = new ArrayList<>();

        for (CreateOrderItem commandItem : command.getItems()) {
            Long productId = skuIdService.extractProductId(commandItem.getSkuId());
            Product product = products.get(productId);
            if (product == null) {
                log.warn("创建订单失败, 商品不存在, customerId={}, storeId={}, skuId={}, productId={}",
                        command.getCustomerId(), command.getStoreId(), commandItem.getSkuId(), productId);
                throw new BizError(OrderErrorCode.PRODUCT_NOT_FOUND, "商品不存在: productId=" + productId);
            }

            OrderItem orderItem = order.addItem(product, commandItem.getSkuId(), commandItem.getQuantity());

            CreateOrderItem item = OrderItemAssembler.toCreateItem(orderItem);
            if (orderItem.getAvailable()) {
                availableItems.add(item);
            } else {
                unavailableItems.add(item);
            }
        }
        if (!unavailableItems.isEmpty()) {
            log.debug("创建订单存在不可售商品, customerId={}, storeId={}, unavailableCount={}, totalCount={}",
                    command.getCustomerId(), command.getStoreId(), unavailableItems.size(), command.getItems().size());
        }

        LocalDateTime paymentExpiredAt = LocalDateTime.now().plusMinutes(PAY_EXPIRE_MINUTES);
        String tradeNo = paymentPort.createTradeNo(CreateTradeRequest.builder()
                .orderNo(order.getOrderNo())
                .buyerOpenId(resolveBuyerOpenId(customer, command.getPaymentMethod()))
                .totalPrice(order.getTotalPrice())
                .totalQuantity(order.getTotalQuantity())
                .paymentMethod(command.getPaymentMethod())
                .payExpireAt(paymentExpiredAt)
                .build());
        order.markCreated(tradeNo, paymentExpiredAt);
        log.debug("调用支付网关创建交易单成功, customerId={}, orderNo={}, tradeNo={}, payExpiredAt={}",
                command.getCustomerId(), order.getOrderNo(), tradeNo, paymentExpiredAt);

        // 数据落库
        try {
            order.save();
            log.debug("订单落库成功, orderNo={}, orderId={}, idempotencyKey={}",
                    order.getOrderNo(), order.getId(), idempotencyKey);
        } catch (DuplicateKeyException e) {
            // MySQL 唯一索引兜底保证幂等键唯一
            if (isIdempotencyKeyConflict(e)) {
                log.warn("创建订单落库触发幂等键唯一约束冲突, 拒绝重复请求, customerId={}, idempotencyKey={}",
                        command.getCustomerId(), idempotencyKey);
                throw new BizError(OrderErrorCode.IDEMPOTENCY_KEY_CONFLICT);
            }
            throw e;
        }

        // 幂等键落库redis
        try {
            idempotencyStore.record(idempotencyKey, order.getOrderNo());
            log.debug("幂等键写入Redis成功, idempotencyKey={}, orderNo={}", idempotencyKey, order.getOrderNo());
        } catch (Exception e) {
            log.error("幂等键写入Redis失败，依赖MySQL唯一索引兜底, idempotencyKey={}, orderNo={}",
                    idempotencyKey, order.getOrderNo(), e);
        }
        
        log.info("创建订单成功, customerId={}, storeId={}, orderNo={}, tradeNo={}, totalPrice={}, totalQuantity={}, availableCount={}, unavailableCount={}",
                command.getCustomerId(), command.getStoreId(), order.getOrderNo(), order.getTradeNo(),
                order.getTotalPrice(), order.getTotalQuantity(), availableItems.size(), unavailableItems.size());

        return OrderCreateResult.builder()
                .available(OrderItemAssembler.toPreBuildItems(availableItems))
                .unavailable(OrderItemAssembler.toPreBuildItems(unavailableItems))
                .totalQuantity(order.getTotalQuantity())
                .totalPrice(order.getTotalPrice())
                .id(order.getId())
                .orderNo(order.getOrderNo())
                .tradeNo(order.getTradeNo())
                .paymentExpiredAt(order.getPaymentExpiredAt())
                .build();
    }

    private boolean isIdempotencyKeyConflict(DuplicateKeyException e) {
        Throwable cause = e.getMostSpecificCause();
        String message = cause == null ? null : cause.getMessage();
        if (message == null || !message.contains("idempotency_key")) {
            return false;
        }
        if (cause instanceof SQLException sqlException) {
            return sqlException.getErrorCode() == 1062;
        }
        return true;
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
