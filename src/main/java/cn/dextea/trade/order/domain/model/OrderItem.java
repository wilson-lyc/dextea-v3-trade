package cn.dextea.trade.order.domain.model;

import cn.dextea.trade.order.domain.exception.OrderErrorCode;
import cn.dextea.trade.shared.domain.error.BizError;
import cn.dextea.trade.shared.domain.model.Money;
import cn.dextea.trade.shared.domain.model.Quantity;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public class OrderItem {
    private Long id;
    private Long orderId;
    private Long productId;
    private String productName;
    private String skuId;
    private String customization;
    private Long coverId;
    private String coverUrl;
    private Quantity quantity;
    private Money unitPrice;
    private Boolean available;

    private OrderItem() {
    }

    public static OrderItem create(Product product, String skuId, Quantity quantity) {
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

        if (skuId != null && skuId.contains("#")) {
            String specPart = skuId.substring(skuId.indexOf('#') + 1);
            if (!specPart.isEmpty()) {
                Map<Long, CustomizationItem> itemMap = product.getCustomization().stream()
                        .collect(Collectors.toMap(CustomizationItem::getId, item -> item));

                List<String[]> pairs = new ArrayList<>();
                for (String pair : specPart.split("-")) {
                    String[] ids = pair.split("_");
                    if (ids.length != 2) {
                        throw new BizError(OrderErrorCode.INVALID_SKU);
                    }
                    pairs.add(ids);
                }

                pairs.sort((a, b) -> Long.compare(parseSkuIdPart(a[0]), parseSkuIdPart(b[0])));

                List<String> segments = new ArrayList<>();
                for (String[] ids : pairs) {
                    long itemId = parseSkuIdPart(ids[0]);
                    long optionId = parseSkuIdPart(ids[1]);

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
        }

        OrderItem orderItem = new OrderItem();
        orderItem.productId = product.getId();
        orderItem.productName = product.getName();
        orderItem.skuId = skuId;
        orderItem.customization = customization;
        orderItem.coverId = product.getCover() != null ? product.getCover().getId() : null;
        orderItem.coverUrl = product.getCover() != null ? product.getCover().getUrl() : null;
        orderItem.quantity = quantity;
        orderItem.unitPrice = product.getPrice().add(customizationPrice);
        orderItem.available = available;
        return orderItem;
    }

    public void assignId(Long id) {
        this.id = id;
    }

    public void assignOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Money getTotalPrice() {
        if (quantity == null || unitPrice == null) {
            return Money.ZERO;
        }
        return unitPrice.multiply(quantity);
    }

    private static long parseSkuIdPart(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new BizError(OrderErrorCode.INVALID_SKU);
        }
    }
}
