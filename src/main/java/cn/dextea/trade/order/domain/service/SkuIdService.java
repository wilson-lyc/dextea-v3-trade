package cn.dextea.trade.order.domain.service;

import cn.dextea.trade.order.domain.model.OrderItem;
import cn.dextea.trade.order.domain.model.Product;
import cn.dextea.trade.shared.domain.quantity.Quantity;

public interface SkuIdService {
    OrderItem idToOrderItem(Product product, String skuId, Quantity quantity);
    Set<Long> getProductIdsFromSkuIds(Set<String> skuIds);
}
