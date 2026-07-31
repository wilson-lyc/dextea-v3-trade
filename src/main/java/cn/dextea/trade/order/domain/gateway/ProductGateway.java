package cn.dextea.trade.order.domain.gateway;
import java.util.List;
import java.util.Map;

import cn.dextea.trade.catalog.domain.model.aggregate.Product;
import cn.dextea.trade.catalog.domain.model.valueobject.ProductCover;
import cn.dextea.trade.catalog.domain.model.valueobject.ProductStoreStatus;
public interface ProductGateway {
    List<Product> findProducts(List<Long> ids);
    List<ProductStoreStatus> findProductStoreStatus(List<Long> productIds, Long storeId);
    Map<Long, ProductCover> findProductCovers(List<Long> productIds);
    Map<Long, String> findCoverUrls(List<Long> coverIds);
}
