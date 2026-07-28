package cn.dextea.trade.order.domain.service;
import cn.dextea.trade.common.error.BizError;
import cn.dextea.trade.order.domain.enums.OrderEventEnum;
import cn.dextea.trade.order.domain.enums.TradeStatusEnum;
import cn.dextea.trade.order.domain.exception.OrderErrorCode;
import cn.dextea.trade.order.domain.gateway.OrderLockGateway;
import cn.dextea.trade.order.domain.gateway.PickupCodeGeneratorGateway;
import cn.dextea.trade.order.domain.model.entity.OrderStatusLog;
import cn.dextea.trade.order.domain.model.aggregate.Order;
import cn.dextea.trade.order.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.util.function.Function;
@Slf4j
@RequiredArgsConstructor
public class OrderStatusDomainService {
    private final OrderRepository orderRepository;
    private final OrderLockGateway orderLockGateway;
    private final PickupCodeGeneratorGateway pickupCodeGeneratorGateway;
    public void markPaid(String orderNo, String tradeNo, LocalDateTime paidAt, String operator) {
        transition(orderNo, operator, OrderEventEnum.PAY,
                order -> order.markPaid(tradeNo, paidAt),
                tradeNo, paidAt, null,
                order -> pickupCodeGeneratorGateway.generate(order.getStoreId()));
    }
    public void markPayTimeout(String orderNo, String operator) {
        transition(orderNo, operator, OrderEventEnum.PAY_TIMEOUT,
                Order::markPayTimeout,
                null, null, null, null);
    }
    public void markRefunded(String orderNo, LocalDateTime refundedAt, String operator) {
        transition(orderNo, operator, OrderEventEnum.REFUND,
                order -> order.markRefunded(refundedAt),
                null, null, refundedAt, null);
    }
    private void transition(String orderNo, String operator, OrderEventEnum event,
                            Function<Order, TradeStatusEnum> mutation,
                            String tradeNo, LocalDateTime paidAt, LocalDateTime refundedAt,
                            Function<Order, String> pickupCodeFn) {
        orderLockGateway.executeWithLock(orderNo, () -> {
            Order order = orderRepository.findByOrderNo(orderNo);
            if (order == null) {
                throw new BizError(OrderErrorCode.ORDER_NOT_FOUND, "订单不存在: " + orderNo);
            }
            TradeStatusEnum currentStatus = order.tradeStatusEnum();
            TradeStatusEnum targetStatus;
            try {
                targetStatus = mutation.apply(order);
            } catch (BizError e) {
                log.warn("非法状态流转被拒绝: orderNo={}, current={}, event={}", orderNo, currentStatus, event);
                throw e;
            }
            String pickupCode = pickupCodeFn == null ? null : pickupCodeFn.apply(order);
            if (pickupCode != null) {
                order.setPickupCode(pickupCode);
                log.info("生成取餐码: orderNo={}, storeId={}, pickupCode={}", orderNo, order.getStoreId(), pickupCode);
            }
            int rows = orderRepository.updateStatusCas(
                    orderNo,
                    targetStatus.getCode(),
                    currentStatus.getCode(),
                    order.getVersion(),
                    tradeNo,
                    paidAt,
                    refundedAt,
                    pickupCode
            );
            if (rows == 0) {
                log.warn("CAS 更新失败（状态已被并发变更）: orderNo={}, expected={}, version={}",
                        orderNo, currentStatus, order.getVersion());
                throw new BizError(OrderErrorCode.ORDER_STATUS_CAS_FAILED, "订单状态已变更，请刷新后重试");
            }
            OrderStatusLog statusLog = OrderStatusLog.builder()
                    .orderId(order.getId())
                    .fromStatus(currentStatus.getCode())
                    .toStatus(targetStatus.getCode())
                    .event(event.name())
                    .operator(operator)
                    .version(order.getVersion() + 1)
                    .build();
            orderRepository.insertStatusLog(statusLog);
            log.info("订单状态变更成功: orderNo={}, {} → {}, event={}, operator={}, version={}",
                    orderNo, currentStatus, targetStatus, event, operator, order.getVersion() + 1);
            return null;
        });
    }
}
