package cn.dextea.trade.order.domain.service.impl;

import cn.dextea.trade.order.domain.exception.SKUDisabledException;
import cn.dextea.trade.order.domain.model.CustomizationItem;
import cn.dextea.trade.order.domain.model.CustomizationOption;
import cn.dextea.trade.order.domain.model.OrderItem;
import cn.dextea.trade.order.domain.model.Product;
import cn.dextea.trade.order.domain.service.SKUService;
import cn.dextea.trade.shared.domain.money.Money;
import cn.dextea.trade.shared.domain.quantity.Quantity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SKUServiceImpl implements SKUService {

    @Override
    public OrderItem createOrderItem(Product product, String skuId, Quantity quantity) {
        List<String> inactiveReasons = new ArrayList<>();
        boolean hasInactive = false;

        if (!product.isActive()) {
            hasInactive = true;
            inactiveReasons.add("商品 " + product.getId() + " 已停用");
        }

        String customization = resolveCustomization(product, skuId, inactiveReasons);
        if (!inactiveReasons.isEmpty()) {
            hasInactive = true;
        }

        Money unitPrice = product.getPrice();
        Money totalPrice = unitPrice.multiply(quantity);
        OrderItem orderItem = OrderItem.builder()
                .productId(product.getId())
                .productName(product.getName())
                .skuId(skuId)
                .customization(customization)
                .coverId(product.getCoverId())
                .quantity(quantity)
                .unitPrice(unitPrice)
                .totalPrice(totalPrice)
                .build();

        if (hasInactive) {
            throw new SKUDisabledException(String.join("; ", inactiveReasons),
                    product.getId(), product.getName(), skuId, customization);
        }
        return orderItem;
    }

    private String resolveCustomization(Product product, String skuId, List<String> inactiveReasons) {
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
                throw new IllegalArgumentException("非法的SKU片段: " + pair);
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
                throw new IllegalArgumentException("SKU中不存在的客制化项目: " + itemId);
            }
            if (!item.isActive()) {
                inactiveReasons.add("客制化项目 " + itemId + " 已停用");
            }
            CustomizationOption option = item.getOptions().stream()
                    .filter(o -> o.getId().equals(optionId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("SKU中不存在的客制化选项: " + optionId));
            if (!option.isActive()) {
                inactiveReasons.add("客制化选项 " + optionId + " 已停用");
            }

            segments.add(item.getName() + "_" + option.getName());
        }
        return String.join("-", segments);
    }
}
