package cn.dextea.trade.order.infrastructure.persistence.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单表（orders）持久化对象：与库表字段一一对应，仅基础设施层可见。
 *
 * <p>领域聚合 {@code Order} 通过 {@code OrderTranslator} 与此类互转，
 * 避免 MyBatis 直接耦合领域模型，隔离持久化细节。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderPO {

    private Long id;

    private String orderNo;

    private String tradeNo;

    private String idempotencyKey;

    private Long customerId;

    private Long storeId;

    /** 交易（支付）状态，对应 orders.trade_status 列 */
    private Integer tradeStatus;

    /** 制作进度状态，对应 orders.making_status 列 */
    private Integer makingStatus;

    /** 乐观锁版本号，对应 orders.version 列 */
    private Integer version;

    private BigDecimal totalPrice;

    private Integer totalQuantity;

    private Integer payMethod;

    private Integer diningMethod;

    private String note;

    /** 支付过期时间点，对应 orders.pay_expire_at 列 */
    private LocalDateTime payExpireAt;

    private LocalDateTime createdAt;

    private LocalDateTime paidAt;

    private LocalDateTime refundedAt;

    private LocalDateTime updatedAt;
}
