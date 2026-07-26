package cn.dextea.trade.order.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 预构建（只读计价）结果（领域值对象），供应用层映射为对外响应或下单落库使用。
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreBuildResult {

    private boolean storeAvailable;

    private boolean customerAvailable;

    private List<UnavailableProduct> unavailableProducts;

    private List<UnavailableCustomization> unavailableCustomizations;

    private List<PricedOrderItem> products;

    private int totalQuantity;

    private BigDecimal totalPrice;
}
