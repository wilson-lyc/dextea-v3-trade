package cn.dextea.trade.order.interfaces.http.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class StoreOrderDetailResponse {

    private Long id;

    private String orderNo;

    private String tradeNo;

    private Long storeId;

    private Integer diningMethod;

    private String note;

    private Integer source;

    private String pickupCode;

    private Integer makingStatus;

    private Integer paymentMethod;

    private Integer paymentStatus;

    private LocalDateTime paymentExpiredAt;

    private LocalDateTime paymentPaidAt;

    private LocalDateTime paymentRefundedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private BigDecimal totalPrice;

    private Integer totalQuantity;

    private List<StoreOrderDetailItemResponse> items;
}
