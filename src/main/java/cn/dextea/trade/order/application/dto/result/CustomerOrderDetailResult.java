package cn.dextea.trade.order.application.dto.result;

import cn.dextea.trade.order.domain.enumeration.DiningMethod;
import cn.dextea.trade.order.domain.enumeration.MakingStatus;
import cn.dextea.trade.order.domain.enumeration.OrderSource;
import cn.dextea.trade.shared.enumeration.PaymentMethod;
import cn.dextea.trade.order.domain.enumeration.PaymentStatus;
import cn.dextea.trade.shared.model.Money;
import cn.dextea.trade.shared.model.Quantity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class CustomerOrderDetailResult {

    private Long id;

    private String orderNo;

    private String tradeNo;

    private Long customerId;

    private Long storeId;

    private DiningMethod diningMethod;

    private String note;

    private OrderSource source;

    private String pickupCode;

    private MakingStatus makingStatus;

    private PaymentMethod paymentMethod;

    private PaymentStatus paymentStatus;

    private LocalDateTime paymentExpiredAt;

    private LocalDateTime paymentPaidAt;

    private LocalDateTime paymentRefundedAt;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Money totalPrice;

    private Quantity totalQuantity;

    private List<CustomerOrderDetailItem> items;
}
