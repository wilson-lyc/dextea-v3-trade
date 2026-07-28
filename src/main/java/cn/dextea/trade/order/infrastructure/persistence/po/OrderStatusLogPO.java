package cn.dextea.trade.order.infrastructure.persistence.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 订单状态变更日志表（order_status_log）持久化对象：与库表字段一一对应，仅基础设施层可见。
 *
 * <p>领域实体 {@code OrderStatusLog} 通过 {@code OrderTranslator} 与此类互转。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusLogPO {

    private Long id;

    /** 订单号，对应 order_status_log.order_no 列 */
    private String orderNo;

    /** 变更前状态码，对应 order_status_log.from_status 列 */
    private Integer fromStatus;

    /** 变更后状态码，对应 order_status_log.to_status 列 */
    private Integer toStatus;

    /** 触发事件名称，对应 order_status_log.event 列 */
    private String event;

    /** 操作人，对应 order_status_log.operator 列 */
    private String operator;

    /** 变更时的版本号，对应 order_status_log.version 列 */
    private Integer version;

    private LocalDateTime createdAt;
}
