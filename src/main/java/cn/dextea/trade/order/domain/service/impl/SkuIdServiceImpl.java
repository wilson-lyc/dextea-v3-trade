package cn.dextea.trade.order.domain.service.impl;

import cn.dextea.trade.order.domain.exception.OrderErrorCode;
import cn.dextea.trade.order.domain.model.CustomizationItem;
import cn.dextea.trade.order.domain.model.CustomizationOption;
import cn.dextea.trade.order.domain.model.OrderItem;
import cn.dextea.trade.order.domain.model.Product;
import cn.dextea.trade.order.domain.service.SkuIdService;
import cn.dextea.trade.shared.domain.error.BizError;
import cn.dextea.trade.shared.domain.money.Money;
import cn.dextea.trade.shared.domain.quantity.Quantity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class SkuIdServiceImpl implements SkuIdService {

    @Override
    public OrderItem buildOrderItem(Product product, String skuId, Quantity quantity) {
        List<String> unavailableReasons = new ArrayList<>();

        if (!product.isActive()) {
            unavailableReasons.add("商品已售罄");
        }

        String customization = resolveCustomization(product, skuId, unavailableReasons);

        boolean available = unavailableReasons.isEmpty();
        Money unitPrice = product.getPrice();
        return OrderItem.builder()
                .productId(product.getId())
                .productName(product.getName())
                .skuId(skuId)
                .customization(customization)
                .cover(product.getCover() != null ? product.getCover().getUrl() : null)
                .quantity(quantity)
                .unitPrice(unitPrice)
                .available(available)
                .unavailableReason(available ? null : unavailableReasons)
                .build();
    }

    @Override
    public Set<Long> extractProductIds(List<String> skuIds) {
        Set<Long> productIds = new HashSet<>();
        for (String skuId : skuIds) {
            if (skuId == null || skuId.isEmpty()) {
                continue;
            }
            String prefix = skuId.contains("#") ? skuId.substring(0, skuId.indexOf('#')) : skuId;
            productIds.add(Long.parseLong(prefix));
        }
        return productIds;
    }

    private String resolveCustomization(Product product, String skuId, List<String> unavailableReasons) {
        if (skuId == null || !skuId.contains("#")) {
            return "";
        }
        String specPart = skuId.substring(skuId.indexOf('#') + 1);
        if (specPart.isEmpty()) {
            return "";
        }

        Map<Long, CustomizationItem> itemMap = product.getCustomization().stream()
                .collect(Collectors.toMap(CustomizationItem::getId, item -> item));

        List<String[]> pairs = new ArrayList<>();
        for (String pair : specPart.split("-")) {
            String[] ids = pair.split("_");
            if (ids.length != 2) {
                throw new BizError(OrderErrorCode.INVALID_SKU, "非法的SKU片段: " + pair);
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
                throw new BizError(OrderErrorCode.INVALID_BINDING, "客制化项目未绑定到该商品: " + itemId);
            }
            if (!item.isActive()) {
                unavailableReasons.add("客制化项目 " + item.getName() + " 已下架");
            }
            CustomizationOption option = item.getOptions().stream()
                    .filter(o -> o.getId().equals(optionId))
                    .findFirst()
                    .orElseThrow(() -> new BizError(OrderErrorCode.INVALID_BINDING, "客制化选项未绑定到该项目: " + optionId));
            if (!option.isActive()) {
                unavailableReasons.add("客制化选项 " + option.getName() + " 已下架");
            }

            segments.add(item.getName() + "_" + option.getName());
        }
        return String.join("-", segments);
    }
}
