package cn.dextea.trade.order.domain.model.valueobject;

import cn.dextea.trade.order.domain.util.SkuIdParser;
import lombok.Getter;

import java.util.List;

@Getter
public class SkuSelection {

    private final String skuId;
    private final int quantity;
    private final Long productId;
    private final List<Long> customizationIds;
    private final List<Long> optionIds;

    private SkuSelection(String skuId, int quantity, Long productId,
                         List<Long> customizationIds, List<Long> optionIds) {
        this.skuId = skuId;
        this.quantity = quantity;
        this.productId = productId;
        this.customizationIds = customizationIds;
        this.optionIds = optionIds;
    }

    public static SkuSelection parse(PreBuildProductInput input) {
        return new SkuSelection(
                input.getSkuId(),
                input.getQuantity(),
                SkuIdParser.parseProductId(input.getSkuId()),
                SkuIdParser.parseItemIds(input.getSkuId()),
                SkuIdParser.parseOptionIds(input.getSkuId()));
    }

    public static List<SkuSelection> parseAll(List<PreBuildProductInput> inputs) {
        return inputs.stream().map(SkuSelection::parse).toList();
    }
}
