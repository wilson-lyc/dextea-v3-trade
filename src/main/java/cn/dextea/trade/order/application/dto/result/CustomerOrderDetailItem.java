package cn.dextea.trade.order.application.dto.result;

import cn.dextea.trade.order.domain.model.OrderItem;
import cn.dextea.trade.shared.model.Money;
import cn.dextea.trade.shared.model.Quantity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
public class CustomerOrderDetailItem {

    private Long id;

    private Long productId;

    private String productName;

    private String skuId;

    private List<String> customization;

    private String coverUrl;

    private Quantity quantity;

    private Money unitPrice;

    private Money totalPrice;

    private Boolean available;

    public static CustomerOrderDetailItem from(OrderItem source) {
        if (source == null) {
            return null;
        }
        return CustomerOrderDetailItem.builder()
                .id(source.getId())
                .productId(source.getProductId())
                .productName(source.getProductName())
                .skuId(source.getSkuId())
                .customization(source.getCustomization())
                .coverUrl(source.getCoverUrl())
                .quantity(source.getQuantity())
                .unitPrice(source.getUnitPrice())
                .totalPrice(source.getTotalPrice())
                .available(source.getAvailable())
                .build();
    }
}
