package cn.dextea.trade.order.infrastructure.persistence.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单明细表（order_items）持久化对象：与库表字段一一对应，仅基础设施层可见。
 *
 * <p>领域实体 {@code OrderItem} 通过 {@code OrderTranslator} 与此类互转。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemPO {

    private Long id;

    private Long orderId;

    private Long productId;

    private String skuId;

    private String productName;

    /** 商品封面标识，对应 order_items.cover_id 列 */
    private Long coverId;

    private Integer quantity;

    private BigDecimal unitPrice;

    private BigDecimal subtotal;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
