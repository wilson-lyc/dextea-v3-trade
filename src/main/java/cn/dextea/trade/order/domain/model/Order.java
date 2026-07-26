package cn.dextea.trade.order.domain.model;

import cn.dextea.trade.common.error.BizError;
import cn.dextea.trade.order.domain.enums.MakingStatusEnum;
import cn.dextea.trade.order.domain.enums.TradeStatusEnum;
import cn.dextea.trade.order.domain.exception.OrderErrorCode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    private Long id;

    private String orderNo;

    private String tradeNo;

    private String idempotencyKey;

    private Long customerId;

    private Long storeId;

    /**
     * 交易（支付）状态，取值见 {@link cn.dextea.trade.order.domain.enums.TradeStatusEnum}，对应库表 trade_status 列。
     */
    private Integer tradeStatus;

    /**
     * 制作进度状态，取值见 {@link cn.dextea.trade.order.domain.enums.MakingStatusEnum}，对应库表 making_status 列。
     * 与支付状态相互独立，描述门店侧制作与交付过程。
     */
    private Integer makingStatus;

    /**
     * 乐观锁版本号，对应库表 version 列。
     * <p>每次状态变更 CAS 更新时 {@code version + 1}，配合 {@code WHERE version = ?} 条件防止 ABA 问题，
     * 是状态不可逆流转的最终原子保障。</p>
     */
    private Integer version;

    private BigDecimal totalPrice;

    private Integer totalQuantity;

    private Integer payMethod;

    private Integer diningMethod;

    private String note;

    private LocalDateTime createdAt;

    private LocalDateTime paidAt;

    private LocalDateTime refundedAt;

    private LocalDateTime updatedAt;

    /**
     * 聚合内订单明细（仅在创建时由应用层装配，查询场景走 {@code OrderRepository} 独立加载）。
     */
    private List<OrderItem> items;

    /**
     * 聚合根工厂：基于预构建结果与下单指令，创建一个处于「待支付」初始态的订单聚合。
     *
     * <p>所有创建期不变式在此集中校验，保证聚合一经创建即处于合法状态：至少含一条明细、
     * 金额非负、数量为正。初始交易状态固定为 {@link TradeStatusEnum#TRADE_WAIT_PAY}，
     * 制作状态固定为 {@link MakingStatusEnum#MAKING_WAIT}，版本号从 0 起算。</p>
     *
     * <p>注：本类仍保留 Lombok {@code @Data} 的 setter，仅为满足 MyBatis 回填主键与
     * Redis 序列化等持久化需要；领域代码应优先通过本工厂与 {@link #markTradeNo} 等
     * 行为方法操作聚合，而非直接调用 setter。</p>
     */
    public static Order createInitial(String orderNo, String idempotencyKey, Long customerId, Long storeId,
                                    int payMethod, int diningMethod, String note,
                                    BigDecimal totalPrice, int totalQuantity, List<OrderItem> items) {
        if (items == null || items.isEmpty()) {
            throw new BizError(OrderErrorCode.ORDER_ITEMS_EMPTY, "订单明细不能为空");
        }
        if (totalPrice == null || totalPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new BizError(OrderErrorCode.ORDER_PRICE_INVALID, "订单金额非法: " + totalPrice);
        }
        if (totalQuantity <= 0) {
            throw new BizError(OrderErrorCode.ORDER_QUANTITY_INVALID, "订单数量非法: " + totalQuantity);
        }
        return Order.builder()
                .orderNo(orderNo)
                .idempotencyKey(idempotencyKey)
                .customerId(customerId)
                .storeId(storeId)
                .tradeStatus(TradeStatusEnum.TRADE_WAIT_PAY.getCode())
                .makingStatus(MakingStatusEnum.MAKING_WAIT.getCode())
                .version(0)
                .payMethod(payMethod)
                .diningMethod(diningMethod)
                .note(note)
                .totalPrice(totalPrice)
                .totalQuantity(totalQuantity)
                .items(items)
                .build();
    }

    /**
     * 回填支付交易号（仅允许从空状态设置一次，防止重复支付下单覆盖既有 trade_no）。
     */
    public void markTradeNo(String tradeNo) {
        if (this.tradeNo != null) {
            throw new BizError(OrderErrorCode.ORDER_TRADE_NO_ALREADY_SET, "trade_no 已存在，不可重复设置");
        }
        this.tradeNo = tradeNo;
    }
}
