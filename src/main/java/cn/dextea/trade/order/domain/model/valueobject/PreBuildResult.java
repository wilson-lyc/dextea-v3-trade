package cn.dextea.trade.order.domain.model.valueobject;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.math.BigDecimal;
import java.util.List;

/**
 * 预构建（只读计价）结果（领域值对象），供应用层映射为对外响应或下单落库使用。
 *
 * <p>作为 {@code OrderCreateResult.preBuild} 的一部分被 Jackson 反序列化（幂等缓存复用），
 * 故需 {@link Jacksonized} 支持 Builder 重建。</p>
 */
@Getter
@Builder
@Jacksonized
public class PreBuildResult {

    private List<UnavailableProduct> unavailableProducts;

    private List<UnavailableCustomization> unavailableCustomizations;

    private List<PricedOrderItem> products;

    private int totalQuantity;

    private BigDecimal totalPrice;

    /**
     * 是否存在不可用项（商品级或客制化级下架/禁用）。
     * <p>作为值对象自身的行为，供应用层在落库前判断是否需要中断下单流程。</p>
     */
    public boolean hasUnavailable() {
        boolean products = unavailableProducts != null && !unavailableProducts.isEmpty();
        boolean customization = unavailableCustomizations != null && !unavailableCustomizations.isEmpty();
        return products || customization;
    }
}
