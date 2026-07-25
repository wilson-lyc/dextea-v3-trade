package cn.dextea.trade.service.impl;

import cn.dextea.trade.entity.Order;
import cn.dextea.trade.entity.OrderStatusLog;
import cn.dextea.trade.enums.OrderEventEnum;
import cn.dextea.trade.enums.TradeStatusEnum;
import cn.dextea.trade.exception.BizError;
import cn.dextea.trade.errorcode.OrderErrorCode;
import cn.dextea.trade.service.OrderLockService;
import cn.dextea.trade.mapper.OrderMapper;
import cn.dextea.trade.mapper.OrderStatusLogMapper;
import cn.dextea.trade.service.OrderStatusService;
import cn.dextea.trade.statemachine.TradeStatusTransitionRules;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderStatusServiceImpl implements OrderStatusService {

    private final OrderMapper orderMapper;
    private final OrderStatusLogMapper orderStatusLogMapper;
    private final OrderLockService orderLockService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changeStatus(String orderNo, OrderEventEnum event, String operator,
                            String tradeNo, LocalDateTime paidAt, LocalDateTime refundedAt) {
        orderLockService.executeWithLock(orderNo, () -> {
            // 1. 查询当前订单
            Order order = orderMapper.selectByOrderNo(orderNo);
            if (order == null) {
                throw new BizError(OrderErrorCode.ORDER_NOT_FOUND, "订单不存在: " + orderNo);
            }

            TradeStatusEnum currentStatus = TradeStatusEnum.of(order.getTradeStatus());

            // 2. 状态机校验：查询 (当前状态, 事件) → 目标状态，不在白名单则拒绝
            TradeStatusEnum targetStatus = TradeStatusTransitionRules.getTarget(currentStatus, event);
            if (targetStatus == null) {
                log.warn("非法状态流转被拒绝: orderNo={}, current={}, event={}", orderNo, currentStatus, event);
                throw new BizError(OrderErrorCode.ORDER_STATUS_TRANSITION_INVALID,
                        String.format("非法状态流转：%s + %s", currentStatus, event));
            }

            // 3. CAS 更新：WHERE order_no=? AND trade_status=? AND version=?
            int rows = orderMapper.updateStatusCas(
                    orderNo,
                    targetStatus.getCode(),
                    currentStatus.getCode(),
                    order.getVersion(),
                    tradeNo,
                    paidAt,
                    refundedAt
            );

            if (rows == 0) {
                // CAS 失败 = 并发冲突或状态已被其他线程修改
                log.warn("CAS 更新失败（状态已被并发变更）: orderNo={}, expected={}, version={}",
                        orderNo, currentStatus, order.getVersion());
                throw new BizError(OrderErrorCode.ORDER_STATUS_CAS_FAILED, "订单状态已变更，请刷新后重试");
            }

            // 4. 记录状态变更日志（审计用）
            OrderStatusLog statusLog = OrderStatusLog.builder()
                    .orderNo(orderNo)
                    .fromStatus(currentStatus.getCode())
                    .toStatus(targetStatus.getCode())
                    .event(event.name())
                    .operator(operator)
                    .version(order.getVersion() + 1)
                    .build();
            orderStatusLogMapper.insert(statusLog);

            log.info("订单状态变更成功: orderNo={}, {} → {}, event={}, operator={}, version={}",
                    orderNo, currentStatus, targetStatus, event, operator, order.getVersion() + 1);

            return null;
        });
    }
}
