package cn.dextea.trade.catalog.domain.model.valueobject;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UnavailableCustomization {
    private Long optionId;
    private String optionName;
    private Long productId;
    private String productName;
    private Long itemId;
    private String itemName;
}
