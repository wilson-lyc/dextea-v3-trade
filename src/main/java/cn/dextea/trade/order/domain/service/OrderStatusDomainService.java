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

/**
 * 订单状态流转领域服务：承载「查询→聚合流转→CAS 更新→写日志→加锁」的编排。
 *
 * <p>对外暴露意图明确的方法（{@link #markPaid}、{@link #markPayTimeout}、{@link #markRefunded}），
 * 状态有向图的合法性由 {@link Order} 聚合根守卫；{@link OrderEventEnum} 仅作为审计日志标签。</p>
 */
@Slf4j
@RequiredArgsConstructor
public class OrderStatusDomainService {

    private final OrderRepository orderRepository;
    private final OrderLockGateway orderLockGateway;
    private final PickupCodeGeneratorGateway pickupCodeGeneratorGateway;

    /**
     * 支付成功：待支付 → 已支付，记录交易号与支付时间，并按门店维度生成取餐码，
     * 取餐码与交易状态在同一条 CAS UPDATE 中落库。
     */
    public void markPaid(String orderNo, String tradeNo, LocalDateTime paidAt, String operator) {
        transition(orderNo, operator, OrderEventEnum.PAY,
                order -> order.markPaid(tradeNo, paidAt),
                tradeNo, paidAt, null,
                order -> pickupCodeGeneratorGateway.generate(order.getStoreId()));
    }

    /**
     * 超时未支付关闭：待支付 → 支付超时。
     */
    public void markPayTimeout(String orderNo, String operator) {
        transition(orderNo, operator, OrderEventEnum.PAY_TIMEOUT,
                Order::markPayTimeout,
                null, null, null, null);
    }

    /**
     * 全额退款完成：已支付 → 已退款，记录退款时间。
     */
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
            // 1. 查询当前订单
            Order order = orderRepository.findByOrderNo(orderNo);
            if (order == null) {
                throw new BizError(OrderErrorCode.ORDER_NOT_FOUND, "订单不存在: " + orderNo);
            }

            TradeStatusEnum currentStatus = order.tradeStatusEnum();

            // 2. 聚合根守卫「有向图」不变式，非法流转直接抛错
            TradeStatusEnum targetStatus;
            try {
                targetStatus = mutation.apply(order);
            } catch (BizError e) {
                log.warn("非法状态流转被拒绝: orderNo={}, current={}, event={}", orderNo, currentStatus, event);
                throw e;
            }

            // 3. 守卫通过后生成取餐码（仅支付路径），与状态变更同一条 UPDATE 落库
            String pickupCode = pickupCodeFn == null ? null : pickupCodeFn.apply(order);
            if (pickupCode != null) {
                order.setPickupCode(pickupCode);
                log.info("生成取餐码: orderNo={}, storeId={}, pickupCode={}", orderNo, order.getStoreId(), pickupCode);
            }

            // 4. CAS 更新：WHERE order_no=? AND trade_status=? AND version=?
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
                // CAS 失败 = 并发冲突或状态已被其他线程修改
                log.warn("CAS 更新失败（状态已被并发变更）: orderNo={}, expected={}, version={}",
                        orderNo, currentStatus, order.getVersion());
                throw new BizError(OrderErrorCode.ORDER_STATUS_CAS_FAILED, "订单状态已变更，请刷新后重试");
            }

            // 5. 记录状态变更日志（审计用）
            OrderStatusLog statusLog = OrderStatusLog.builder()
                    .orderNo(orderNo)
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
