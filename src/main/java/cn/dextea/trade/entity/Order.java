package cn.dextea.trade.entity;

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

    private String orderNo;

    private String tradeNo;

    private String idempotencyKey;

    private Long customerId;

    private Long storeId;

    /**
     * 交易（支付）状态，取值见 {@link cn.dextea.trade.enums.TradeStatusEnum}，对应库表 trade_status 列。
     */
    private Integer tradeStatus;

    /**
     * 制作进度状态，取值见 {@link cn.dextea.trade.enums.MakingStatusEnum}，对应库表 making_status 列。
     * 与支付状态相互独立，描述门店侧制作与交付过程。
     */
    private Integer makingStatus;

    private BigDecimal totalPrice;

    private Integer totalQuantity;

    private Integer payMethod;

    private Integer diningMethod;

    private String note;

    private LocalDateTime createdAt;

    private LocalDateTime paidAt;

    private LocalDateTime refundedAt;

    private LocalDateTime updatedAt;
}
