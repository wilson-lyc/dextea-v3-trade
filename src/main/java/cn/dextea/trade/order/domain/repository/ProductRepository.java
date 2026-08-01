package cn.dextea.trade.order.domain.repository;

import cn.dextea.trade.order.domain.model.aggregate.Product;

import java.util.List;
import java.util.Map;

public interface ProductRepository {
    Map<Long, Product> getProductByIdsWithStoreId(List<Long> ids, Long storeId);
}
