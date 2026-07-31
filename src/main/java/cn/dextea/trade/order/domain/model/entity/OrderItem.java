package cn.dextea.trade.order.domain.model.entity;

import cn.dextea.trade.shared.domain.money.Money;
import cn.dextea.trade.shared.domain.quantity.Quantity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    private Long id;
    private Long orderId;
    private Long productId;
    private String productName;
    private String skuId;
    private String customization;
    private Long coverId;
    private Quantity quantity;
    private Money unitPrice;
    private Money subtotal;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
