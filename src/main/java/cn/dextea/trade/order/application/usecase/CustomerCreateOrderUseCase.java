package cn.dextea.trade.order.application.usecase;

import cn.dextea.trade.order.application.assembler.CustomerOrderItemAssembler;
import cn.dextea.trade.order.application.dto.command.CustomerCreateOrderCommand;
import cn.dextea.trade.order.application.dto.result.CustomerOrderCreateResult;
import cn.dextea.trade.order.application.dto.shared.CustomerCreateOrderItem;
import cn.dextea.trade.order.domain.exception.OrderErrorCode;
import cn.dextea.trade.order.domain.model.Order;
import cn.dextea.trade.order.domain.model.OrderItem;
import cn.dextea.trade.order.domain.model.SkuItem;
import cn.dextea.trade.order.domain.port.IdempotencyStore;
import cn.dextea.trade.order.domain.service.OrderCreationService;
import cn.dextea.trade.shared.error.BizError;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerCreateOrderUseCase {

    private final OrderCreationService orderCreationService;
    private final IdempotencyStore idempotencyStore;

    public CustomerOrderCreateResult execute(CustomerCreateOrderCommand command) {
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

        // 二次校验幂等键，防止并发穿透
        if (idempotencyStore.exists(idempotencyKey)) {
            log.warn("创建订单幂等键已存在(二次校验), 拒绝重复请求, customerId={}, idempotencyKey={}",
                    customerId, idempotencyKey);
            throw new BizError(OrderErrorCode.IDEMPOTENCY_KEY_CONFLICT);
        }
        return doCreate(command);
    }

    private CustomerOrderCreateResult doCreate(CustomerCreateOrderCommand command) {
        String idempotencyKey = command.getIdempotencyKey();
        List<SkuItem> skuItems = CustomerOrderItemAssembler.toSkuItems(command.getItems());

        Order order;
        try {
            order = orderCreationService.createOrder(
                    command.getCustomerId(), command.getStoreId(), skuItems,
                    command.getSource(), command.getPaymentMethod(), command.getDiningMethod(),
                    command.getNote(), idempotencyKey);
        } catch (DuplicateKeyException e) {
            // MySQL 唯一索引兜底保证幂等键唯一
            if (isIdempotencyKeyConflict(e)) {
                log.warn("创建订单落库触发幂等键唯一约束冲突, 拒绝重复请求, customerId={}, idempotencyKey={}",
                        command.getCustomerId(), idempotencyKey);
                throw new BizError(OrderErrorCode.IDEMPOTENCY_KEY_CONFLICT);
            }
            throw e;
        }

        // 存在不可售商品时走预构建结果：不生成订单号、不创建交易、不落库、不记录幂等键，
        // 直接返回订单数据，由前端识别不可下单的商品
        if (!order.isCreated()) {
            log.warn("创建订单降级为预构建结果, 不记录幂等键, customerId={}, storeId={}, idempotencyKey={}",
                    command.getCustomerId(), command.getStoreId(), idempotencyKey);
            return toResult(order);
        }

        // 幂等键写入 Redis
        try {
            idempotencyStore.record(idempotencyKey, order.getOrderNo());
            log.debug("幂等键写入Redis成功, idempotencyKey={}, orderNo={}", idempotencyKey, order.getOrderNo());
        } catch (Exception e) {
            log.error("幂等键写入Redis失败，依赖MySQL唯一索引兜底, idempotencyKey={}, orderNo={}",
                    idempotencyKey, order.getOrderNo(), e);
        }

        CustomerOrderCreateResult result = toResult(order);

        log.info("创建订单成功, customerId={}, storeId={}, orderNo={}, tradeNo={}, totalPrice={}, totalQuantity={}, availableCount={}, unavailableCount={}",
                command.getCustomerId(), command.getStoreId(), order.getOrderNo(), order.getTradeNo(),
                order.getTotalPrice(), order.getTotalQuantity(),
                result.getAvailable().size(), result.getUnavailable().size());

        return result;
    }

    private CustomerOrderCreateResult toResult(Order order) {
        // 按可售状态分组
        List<CustomerCreateOrderItem> availableItems = new ArrayList<>();
        List<CustomerCreateOrderItem> unavailableItems = new ArrayList<>();
        for (OrderItem orderItem : order.getItems()) {
            CustomerCreateOrderItem item = CustomerOrderItemAssembler.toCreateItem(orderItem);
            if (orderItem.getAvailable()) {
                availableItems.add(item);
            } else {
                unavailableItems.add(item);
            }
        }

        return CustomerOrderCreateResult.builder()
                .available(CustomerOrderItemAssembler.toPreBuildItems(availableItems))
                .unavailable(CustomerOrderItemAssembler.toPreBuildItems(unavailableItems))
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
        if (cause instanceof SQLException sqlException) {
            return sqlException.getErrorCode() == 1062;
        }
        return false;
    }
}
