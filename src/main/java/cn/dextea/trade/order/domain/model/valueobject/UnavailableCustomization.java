package cn.dextea.trade.order.domain.model.valueobject;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
@Getter
@Builder
@Jacksonized
public class UnavailableCustomization {
    private Long optionId;
    private String optionName;
    private Long productId;
    private String productName;
    private Long itemId;
    private String itemName;
}
