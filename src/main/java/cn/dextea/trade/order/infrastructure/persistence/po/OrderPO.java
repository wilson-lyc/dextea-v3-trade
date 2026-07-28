package cn.dextea.trade.order.infrastructure.persistence.po;
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
public class OrderPO {
    private Long id;
    private String orderNo;
    private String tradeNo;
    private String idempotencyKey;
    private Long customerId;
    private Long storeId;
    private Integer tradeStatus;
    private Integer makingStatus;
    private String pickupCode;
    private Integer version;
    private BigDecimal totalPrice;
    private Integer totalQuantity;
    private Integer payMethod;
    private Integer diningMethod;
    private String note;
    private LocalDateTime payExpireAt;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
    private LocalDateTime refundedAt;
    private LocalDateTime updatedAt;
}
