package cn.dextea.trade.order.domain.model.aggregate;

import cn.dextea.trade.shared.domain.error.BizError;
import cn.dextea.trade.order.domain.exception.OrderErrorCode;
import cn.dextea.trade.order.domain.model.entity.OrderItem;
import cn.dextea.trade.order.domain.model.valueobject.DiningMethod;
import cn.dextea.trade.order.domain.model.valueobject.MakingStatus;
import cn.dextea.trade.order.domain.model.valueobject.OrderNumber;
import cn.dextea.trade.order.domain.model.valueobject.PaymentMethod;
import cn.dextea.trade.order.domain.model.valueobject.PaymentStatus;
import cn.dextea.trade.order.domain.model.valueobject.PickupCode;
import cn.dextea.trade.order.domain.model.valueobject.PreBuildResult;
import cn.dextea.trade.shared.domain.money.Money;
import cn.dextea.trade.shared.domain.quantity.Quantity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.Duration;
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
    private Money totalPrice;
    private Quantity totalQuantity;
    private DiningMethod diningMethod;
    private String note;
    private String pickupCode;
    private MakingStatus makingStatus;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private LocalDateTime paymentExpiredAt;
    private LocalDateTime paymentPaidAt;
    private LocalDateTime paymentRefundedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer version;
    private List<OrderItem> items;
}
