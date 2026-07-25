package cn.dextea.trade.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 订单状态变更日志（审计用）。
 *
 * <p>每次通过 {@link cn.dextea.trade.service.OrderStatusService#changeStatus} 成功变更状态后，
 * 都会插入一条记录，完整留存「谁、在什么时间、把订单从哪个状态、经什么事件、变更为哪个状态」。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusLog {

    private Long id;

    /** 订单号（与 orders.order_no 对齐，便于按业务键检索） */
    private String orderNo;

    /** 变更前状态码，取值见 {@link cn.dextea.trade.enums.TradeStatusEnum} */
    private Integer fromStatus;

    /** 变更后状态码 */
    private Integer toStatus;

    /** 触发事件名称，取值见 {@link cn.dextea.trade.enums.OrderEventEnum} */
    private String event;

    /** 操作人（如 system-pay-callback、user、admin 等） */
    private String operator;

    /** 变更时的版本号（变更后的版本） */
    private Integer version;

    private LocalDateTime createdAt;
}
