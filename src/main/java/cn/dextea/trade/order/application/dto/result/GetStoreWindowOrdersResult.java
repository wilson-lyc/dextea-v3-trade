package cn.dextea.trade.order.application.dto.result;

import cn.dextea.trade.order.domain.enumeration.DiningMethod;
import cn.dextea.trade.order.domain.enumeration.MakingStatus;
import cn.dextea.trade.order.domain.enumeration.PaymentStatus;
import cn.dextea.trade.shared.model.Money;
import cn.dextea.trade.shared.model.Quantity;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetStoreWindowOrdersResult {

    private List<StoreWindowOrderItem> items;

    private Long total;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StoreWindowOrderItem {

        private Long orderId;

        private String orderNo;

        private String pickupCode;

        private Money totalPrice;

        private Quantity totalQuantity;

        private DiningMethod diningMethod;

        private MakingStatus makingStatus;

        private PaymentStatus paymentStatus;

        private LocalDateTime createdAt;
    }
}
