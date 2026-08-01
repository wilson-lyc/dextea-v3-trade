package cn.dextea.trade.order.domain.repository;

import cn.dextea.trade.order.domain.model.aggregate.Product;

import java.util.Map;
import java.util.Set;

public interface ProductRepository {
    Map<Long, Product> getProductByIdsWithStoreId(Set<Long> ids, Long storeId);
}
