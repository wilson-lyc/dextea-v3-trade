package cn.dextea.trade.order.application.dto.shared;

import cn.dextea.trade.shared.domain.money.Money;
import cn.dextea.trade.shared.domain.quantity.Quantity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class AbstractOrderItem {
    private String skuId;
    private Quantity quantity;
    private String product;
    private String customization;
    private String cover;
    private Money unitPrice;
    private Money totalPrice;
    private Boolean available;
}
