package cn.dextea.trade.order.domain.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 订单状态变更日志（审计用）。
 *
 * <p>每次通过 {@code OrderStatusDomainService#changeStatus} 成功变更状态后，
 * 都会插入一条记录，完整留存「谁、在什么时间、把订单从哪个状态、经什么事件、变更为哪个状态」。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusLog {

    private Long id;

    /** 订单ID（内部数据流转标识，对应 orders 表的 order_no 列） */
    private String orderId;

    /** 变更前状态码，取值见 {@link cn.dextea.trade.order.domain.enums.TradeStatusEnum} */
    private Integer fromStatus;

    /** 变更后状态码 */
    private Integer toStatus;

    /** 触发事件名称，取值见 {@link cn.dextea.trade.order.domain.enums.OrderEventEnum} */
    private String event;

    /** 操作人（如 system-pay-callback、user、admin 等） */
    private String operator;

    /** 变更时的版本号（变更后的版本） */
    private Integer version;

    private LocalDateTime createdAt;
}
