package cn.dextea.trade.order.domain.port;

import cn.dextea.trade.catalog.domain.model.Customization;
import cn.dextea.trade.catalog.domain.model.CustomizationOption;
import cn.dextea.trade.catalog.domain.model.CustomizationOptionStoreStatus;
import cn.dextea.trade.catalog.domain.model.Customer;
import cn.dextea.trade.catalog.domain.model.Gallery;
import cn.dextea.trade.catalog.domain.model.Product;
import cn.dextea.trade.catalog.domain.model.ProductImage;
import cn.dextea.trade.catalog.domain.model.ProductStoreStatus;
import cn.dextea.trade.catalog.domain.model.Store;

import java.util.List;

/**
 * 商品目录防腐端口：以只读快照形式向订单领域提供商品/客制化/门店/顾客等支撑数据。
 *
 * <p>门店与顾客同属 catalog 参考数据，统一经此端口获取，避免在订单域散落多套等价端口。
 * 订单领域不直接依赖 catalog 持久化细节，仅通过该端口获取领域所需的参考快照。</p>
 */
public interface CatalogPort {

    List<Product> findProducts(List<Long> ids);

    List<ProductImage> findCoverImages(List<Long> productIds);

    List<Gallery> findGalleries(List<Long> imageIds);

    List<ProductStoreStatus> findProductStoreStatus(List<Long> productIds, Long storeId);

    List<Customization> findCustomizations(List<Long> ids);

    List<CustomizationOption> findOptions(List<Long> ids);

    List<CustomizationOptionStoreStatus> findOptionStoreStatus(List<Long> optionIds, Long storeId);

    /** 门店只读快照（用于下单前门店可用性校验）。 */
    Store findStore(Long id);

    /** 顾客只读快照（用于下单前顾客可用性校验与支付绑卡）。 */
    Customer findCustomer(Long id);
}
