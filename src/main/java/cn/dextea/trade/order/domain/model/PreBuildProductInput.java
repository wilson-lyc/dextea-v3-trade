package cn.dextea.trade.order.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 预构建输入中的单个商品项（领域值对象）。
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreBuildProductInput {

    private String skuId;

    private Integer quantity;
}
