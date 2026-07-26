package cn.dextea.trade.order.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 不可用的商品（领域值对象）。
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnavailableProduct {

    private Long id;

    private String name;
}
