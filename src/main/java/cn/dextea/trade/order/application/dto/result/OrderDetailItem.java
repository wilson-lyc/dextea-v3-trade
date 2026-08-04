package cn.dextea.trade.order.application.dto.result;

import cn.dextea.trade.shared.domain.model.Money;
import cn.dextea.trade.shared.domain.model.Quantity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class OrderDetailItem {

    private Long id;

    private Long productId;

    private String productName;

    private String skuId;

    private String customization;

    private String coverUrl;

    private Quantity quantity;

    private Money unitPrice;

    private Money totalPrice;

    private Boolean available;
}
