package cn.dextea.trade.order.domain.port;

import cn.dextea.trade.order.domain.model.aggregate.Product;
import cn.dextea.trade.order.domain.model.valueobject.ProductCover;
import cn.dextea.trade.order.domain.model.valueobject.ProductStoreStatus;

import java.util.List;
import java.util.Map;

public interface ProductRepository {

    List<Product> findProducts(List<Long> ids);

    List<ProductStoreStatus> findProductStoreStatus(List<Long> productIds, Long storeId);

    Map<Long, ProductCover> findProductCovers(List<Long> productIds);

    Map<Long, String> findCoverUrls(List<Long> coverIds);
}
