package cn.dextea.trade.entity;

import cn.dextea.trade.entity.enums.OrderStatus;
import cn.dextea.trade.entity.enums.PayMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import me.ahoo.cosid.annotation.CosId;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    private Long id;

    /**
     * 订单号：由 CosId 的 MyBatis 拦截器在 insert 时自动注入雪花 ID（String）。
     * 字段为空时自动生成，非空则跳过。
     */
    @CosId
    private String orderNo;

    private String tradeNo;

    /**
     * 幂等键：由前端生成的 UUID，一次结算会话内复用。
     * 数据库层对该列建唯一索引，作为订单创建的最终兜底。
     */
    private String idempotencyKey;

    private Long customerId;

    private Long storeId;

    private Integer status;

    private BigDecimal price;

    /**
     * 订单商品总数量。
     */
    private Integer quantity;

    private Integer payMethod;

    private String remark;

    private LocalDateTime createdAt;

    private LocalDateTime paidAt;

    private LocalDateTime refundedAt;

    private LocalDateTime updatedAt;
}
