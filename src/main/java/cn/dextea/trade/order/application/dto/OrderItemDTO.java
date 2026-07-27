package cn.dextea.trade.order.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 订单详情中的商品明细（应用层 DTO）。
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDTO {

    private Long productId;

    private String productName;

    private String skuId;

    private String coverUrl;

    private String customizationText;

    private Integer quantity;

    private BigDecimal unitPrice;

    private BigDecimal subtotal;
}
