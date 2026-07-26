package cn.dextea.trade.order.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 订单详情中的商品明细视图（领域值对象）。
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailItemView {

    private Long productId;

    private String productName;

    private String skuId;

    private String coverUrl;

    private String customizationText;

    private Integer quantity;

    private BigDecimal unitPrice;

    private BigDecimal subtotal;
}
