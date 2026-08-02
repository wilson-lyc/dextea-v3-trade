package cn.dextea.trade.order.domain.service;

import cn.dextea.trade.order.domain.model.OrderItem;
import cn.dextea.trade.order.domain.model.Product;
import cn.dextea.trade.shared.domain.quantity.Quantity;

public interface SkuIdService {
    OrderItem buildOrderItem(Product product, String skuId, Quantity quantity);
    Set<Long> extractProductIds(List<String> skuIds);
}
