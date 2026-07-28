package cn.dextea.trade.order.application.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailDTO {
    private Long id;
    private String orderNo;
    private String tradeNo;
    private Integer tradeStatus;
    private Integer makingStatus;
    private String pickupCode;
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
    private Long storeId;
    private String storeName;
    private List<OrderItemDTO> items;
}
