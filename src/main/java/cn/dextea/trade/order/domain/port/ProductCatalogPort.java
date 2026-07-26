package cn.dextea.trade.order.domain.port;

import cn.dextea.trade.catalog.domain.model.Customization;
import cn.dextea.trade.catalog.domain.model.CustomizationOption;
import cn.dextea.trade.catalog.domain.model.CustomizationOptionStoreStatus;
import cn.dextea.trade.catalog.domain.model.Gallery;
import cn.dextea.trade.catalog.domain.model.Product;
import cn.dextea.trade.catalog.domain.model.ProductImage;
import cn.dextea.trade.catalog.domain.model.ProductStoreStatus;

import java.util.List;

/**
 * 商品目录防腐端口：以只读快照形式向订单领域提供商品/客制化/封面等支撑数据。
 *
 * <p>订单领域不直接依赖 catalog 持久化细节，仅通过该端口获取领域所需的参考数据。</p>
 */
public interface ProductCatalogPort {

    List<Product> findProducts(List<Long> ids);

    List<ProductImage> findCoverImages(List<Long> productIds);

    List<Gallery> findGalleries(List<Long> imageIds);

    List<ProductStoreStatus> findProductStoreStatus(List<Long> productIds, Long storeId);

    List<Customization> findCustomizations(List<Long> ids);

    List<CustomizationOption> findOptions(List<Long> ids);

    List<CustomizationOptionStoreStatus> findOptionStoreStatus(List<Long> optionIds, Long storeId);
}
