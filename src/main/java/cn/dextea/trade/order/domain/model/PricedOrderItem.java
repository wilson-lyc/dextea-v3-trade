package cn.dextea.trade.order.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 预构建阶段计价完成的有效商品明细（领域值对象）。
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PricedOrderItem {

    private String skuId;

    private Integer quantity;

    private Long productId;

    private String productName;

    private Long coverId;

    private String coverUrl;

    private String customizationText;

    private BigDecimal unitPrice;

    private BigDecimal subtotal;
}
