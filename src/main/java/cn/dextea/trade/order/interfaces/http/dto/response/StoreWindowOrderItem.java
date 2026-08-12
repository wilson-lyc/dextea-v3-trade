package cn.dextea.trade.order.interfaces.http.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreWindowOrderItem {

    private Long orderId;

    private String orderNo;

    private BigDecimal totalPrice;

    private Integer totalQuantity;

    private Integer diningMethod;

    private String note;

    private Integer makingStatus;

    private Integer paymentMethod;

    private Integer paymentStatus;

    private LocalDateTime createdAt;
}
