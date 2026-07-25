package cn.dextea.trade.service;

import cn.dextea.trade.enums.OrderEventEnum;

import java.time.LocalDateTime;

/**
 * 订单状态变更统一入口。
 *
 * <p>所有订单交易状态的变更都必须经此接口，内部组合三层防护：
 * <ol>
 *     <li>Redis 分布式锁 —— 串行化同一订单的并发请求</li>
 *     <li>状态机白名单 —— 校验 {@code (当前状态, 事件)} 是否合法流转</li>
 *     <li>数据库 CAS UPDATE —— 原子落盘，{@code WHERE status=? AND version=?} 防并发与 ABA</li>
 * </ol>
 * 任何一层出问题，后面的层都能兜住，构成纵深防御，从根本上杜绝「已支付倒退到待支付」。</p>
 */
public interface OrderStatusService {

    /**
     * 变更订单状态（统一入口）。
     *
     * <p>流程：加锁 → 查询当前状态 → 状态机校验 → CAS 更新 → 记录审计日志 → 释放锁。
     * CAS 失败或流转非法时抛出 {@link cn.dextea.trade.exception.BizError}。</p>
     *
     * @param orderNo    订单号
     * @param event      触发事件
     * @param operator   操作人（如 system-pay-callback、user）
     * @param tradeNo    支付平台交易号（支付事件回填，其他事件传 null）
     * @param paidAt     支付时间（支付事件回填，其他事件传 null）
     * @param refundedAt 退款时间（退款事件回填，其他事件传 null）
     */
    void changeStatus(String orderNo, OrderEventEnum event, String operator,
                      String tradeNo, LocalDateTime paidAt, LocalDateTime refundedAt);

    /**
     * 变更订单状态（简化重载，不回填交易号/时间字段）。
     *
     * @param orderNo  订单号
     * @param event    触发事件
     * @param operator 操作人
     */
    default void changeStatus(String orderNo, OrderEventEnum event, String operator) {
        changeStatus(orderNo, event, operator, null, null, null);
    }
}
