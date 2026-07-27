package cn.dextea.trade.order.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

/**
 * 不可用的商品（领域值对象）。
 *
 * <p>作为 {@code PreBuildResult.unavailableProducts} 的一部分被 Jackson 反序列化（幂等缓存复用），
 * 故需 {@link Jacksonized} 支持 Builder 重建。</p>
 */
@Getter
@Builder
@Jacksonized
public class UnavailableProduct {

    private Long id;

    private String name;
}
