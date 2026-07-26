package cn.dextea.trade.order.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 不可用的客制化项（领域值对象）。
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnavailableCustomization {

    private Long optionId;

    private String optionName;

    private Long productId;

    private String productName;

    private Long itemId;

    private String itemName;
}
