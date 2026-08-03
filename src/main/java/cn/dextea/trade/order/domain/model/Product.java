package cn.dextea.trade.order.domain.model;

import cn.dextea.trade.order.domain.exception.OrderErrorCode;
import cn.dextea.trade.order.domain.model.enums.ProductGlobalStatus;
import cn.dextea.trade.order.domain.model.enums.ProductStoreStatus;
import cn.dextea.trade.shared.domain.error.BizError;
import cn.dextea.trade.shared.domain.model.Money;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    private Long id;
    private String name;
    private ProductGlobalStatus globalStatus;
    private ProductStoreStatus storeStatus;
    private Money price;
    private ProductCover cover;
    private List<CustomizationItem> customization;

    public boolean isActive() {
        return globalStatus == ProductGlobalStatus.ACTIVE
                && storeStatus == ProductStoreStatus.ACTIVE;
    }

    public String resolveCustomization(String skuId, AtomicBoolean available) {
        if (skuId == null || !skuId.contains("#")) {
            return "";
        }
        String specPart = skuId.substring(skuId.indexOf('#') + 1);
        if (specPart.isEmpty()) {
            return "";
        }

        Map<Long, CustomizationItem> itemMap = customization.stream()
                .collect(Collectors.toMap(CustomizationItem::getId, item -> item));

        List<String[]> pairs = new ArrayList<>();
        for (String pair : specPart.split("-")) {
            String[] ids = pair.split("_");
            if (ids.length != 2) {
                throw new BizError(OrderErrorCode.INVALID_SKU);
            }
            pairs.add(ids);
        }

        pairs.sort((a, b) -> Long.compare(Long.parseLong(a[0]), Long.parseLong(b[0])));

        List<String> segments = new ArrayList<>();
        for (String[] ids : pairs) {
            long itemId = Long.parseLong(ids[0]);
            long optionId = Long.parseLong(ids[1]);

            CustomizationItem item = itemMap.get(itemId);
            if (item == null) {
                throw new BizError(OrderErrorCode.INVALID_BINDING, "客制化项目未绑定到该商品: productId=" + id + ", itemId=" + itemId);
            }
            CustomizationOption option = item.getOptions().stream()
                    .filter(o -> o.getId().equals(optionId))
                    .findFirst()
                    .orElseThrow(() -> new BizError(OrderErrorCode.INVALID_BINDING, "客制化选项未绑定到该项目: productId=" + id + ", itemId=" + itemId + ", optionId=" + optionId));

            if (!item.isActive() || !option.isActive()) {
                available.set(false);
            }

            segments.add(item.getName() + "_" + option.getName());
        }
        return String.join("-", segments);
    }
}
