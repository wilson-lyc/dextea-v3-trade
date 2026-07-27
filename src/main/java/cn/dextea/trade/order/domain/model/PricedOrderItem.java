package cn.dextea.trade.order.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.math.BigDecimal;

/**
 * 预构建阶段计价完成的有效商品明细（领域值对象）。
 *
 * <p>作为 {@code PreBuildResult.products} 的一部分被 Jackson 反序列化（幂等缓存复用），
 * 故需 {@link Jacksonized} 支持 Builder 重建。</p>
 */
@Getter
@Builder
@Jacksonized
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
