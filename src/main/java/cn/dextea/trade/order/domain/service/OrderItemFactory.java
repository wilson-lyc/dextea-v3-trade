package cn.dextea.trade.order.domain.service;

import cn.dextea.trade.order.domain.exception.OrderErrorCode;
import cn.dextea.trade.order.domain.model.CustomizationItem;
import cn.dextea.trade.order.domain.model.CustomizationOption;
import cn.dextea.trade.order.domain.model.OrderItem;
import cn.dextea.trade.order.domain.model.Product;
import cn.dextea.trade.shared.error.BizError;
import cn.dextea.trade.shared.model.Money;
import cn.dextea.trade.shared.model.Quantity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OrderItemFactory {

    private final SkuIdParser skuIdParser;

    public OrderItem create(Product product, String skuId, Quantity quantity) {
        if (product == null) {
            throw new BizError(OrderErrorCode.PRODUCT_NOT_FOUND);
        }
        if (skuId == null || skuId.isEmpty()) {
            throw new BizError(OrderErrorCode.INVALID_SKU);
        }
        if (quantity == null || quantity.equals(Quantity.ZERO)) {
            throw new BizError(OrderErrorCode.INVALID_ORDER_ITEM_QUANTITY);
        }

        boolean available = product.isActive();
        String customization = "";
        Money customizationPrice = Money.ZERO;

        List<long[]> pairs = skuIdParser.parseCustomizationPairs(skuId);
        if (!pairs.isEmpty()) {
            Map<Long, CustomizationItem> itemMap = product.getCustomization().stream()
                    .collect(Collectors.toMap(CustomizationItem::getId, item -> item));

            List<String> segments = new ArrayList<>();
            for (long[] ids : pairs) {
                long itemId = ids[0];
                long optionId = ids[1];

                CustomizationItem item = itemMap.get(itemId);
                if (item == null) {
                    throw new BizError(OrderErrorCode.INVALID_BINDING, "客制化项目未绑定到该商品: productId=" + product.getId() + ", itemId=" + itemId);
                }
                CustomizationOption option = item.getOptions().stream()
                        .filter(o -> o.getId().equals(optionId))
                        .findFirst()
                        .orElseThrow(() -> new BizError(OrderErrorCode.INVALID_BINDING, "客制化选项未绑定到该项目: productId=" + product.getId() + ", itemId=" + itemId + ", optionId=" + optionId));

                if (!item.isActive() || !option.isActive()) {
                    available = false;
                }

                if (option.getPrice() != null) {
                    customizationPrice = customizationPrice.add(option.getPrice());
                }

                segments.add(item.getName() + "_" + option.getName());
            }
            customization = String.join("-", segments);
        }

        String coverUrl = product.getCover() != null ? product.getCover().getUrl() : null;
        Money unitPrice = product.getPrice().add(customizationPrice);

        return OrderItem.create(product.getId(), product.getName(), skuId, customization,
                coverUrl, quantity, unitPrice, available);
    }
}
