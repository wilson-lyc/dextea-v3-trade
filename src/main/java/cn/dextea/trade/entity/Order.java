package cn.dextea.trade.entity;

import cn.dextea.trade.entity.enums.OrderStatus;
import cn.dextea.trade.entity.enums.PayMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    private Long id;

    /**
     * 订单号：业务代码在创建订单时显式生成（雪花 ID，String），不再依赖 INSERT 时由框架注入。
     */
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
