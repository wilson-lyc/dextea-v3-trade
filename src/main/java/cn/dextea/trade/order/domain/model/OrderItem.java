package cn.dextea.trade.order.domain.model;

import cn.dextea.trade.shared.domain.money.Money;
import cn.dextea.trade.shared.domain.quantity.Quantity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    private Long id;
    private Long productId;
    private String productName;
    private String skuId;
    private String customization;
    private String cover;
    private Quantity quantity;
    private Money unitPrice;
    private Boolean available;

    public Money getTotalPrice() {
        if (quantity == null || unitPrice == null) {
            return Money.ZERO;
        }
        return unitPrice.multiply(quantity);
    }
}
