package cn.dextea.trade.order.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 订单状态变更事件标签：仅用于 {@code OrderStatusLog} 审计日志与运行日志，
 * 不参与状态流转判定（流转规则由 {@code Order} 聚合根的行为方法守卫）。
 */
@Getter
@RequiredArgsConstructor
public enum OrderEventEnum {

    PAY("支付成功"),
    PAY_TIMEOUT("超时未支付关闭"),
    REFUND("全额退款");

    private final String description;
}
