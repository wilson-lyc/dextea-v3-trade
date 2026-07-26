package cn.dextea.trade.order.infrastructure.adapter;

import cn.dextea.trade.catalog.domain.model.Customization;
import cn.dextea.trade.catalog.domain.model.CustomizationOption;
import cn.dextea.trade.catalog.domain.model.CustomizationOptionStoreStatus;
import cn.dextea.trade.catalog.domain.model.Gallery;
import cn.dextea.trade.catalog.domain.model.Product;
import cn.dextea.trade.catalog.domain.model.ProductImage;
import cn.dextea.trade.catalog.domain.model.ProductStoreStatus;
import cn.dextea.trade.catalog.domain.service.CatalogQueryService;
import cn.dextea.trade.order.domain.port.ProductCatalogPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 商品目录防腐适配器：实现订单领域 {@link ProductCatalogPort}，委托 catalog 查询服务获取只读快照。
 */
@Component
@RequiredArgsConstructor
public class ProductCatalogAdapter implements ProductCatalogPort {

    private final CatalogQueryService catalogQueryService;

    @Override
    public List<Product> findProducts(List<Long> ids) {
        return catalogQueryService.findProductsByIds(ids);
    }

    @Override
    public List<ProductImage> findCoverImages(List<Long> productIds) {
        return catalogQueryService.findCoverImagesByProductIds(productIds);
    }

    @Override
    public List<Gallery> findGalleries(List<Long> imageIds) {
        return catalogQueryService.findGalleriesByIds(imageIds);
    }

    @Override
    public List<ProductStoreStatus> findProductStoreStatus(List<Long> productIds, Long storeId) {
        return catalogQueryService.findProductStoreStatus(productIds, storeId);
    }

    @Override
    public List<Customization> findCustomizations(List<Long> ids) {
        return catalogQueryService.findCustomizationsByIds(ids);
    }

    @Override
    public List<CustomizationOption> findOptions(List<Long> ids) {
        return catalogQueryService.findCustomizationOptionsByIds(ids);
    }

    @Override
    public List<CustomizationOptionStoreStatus> findOptionStoreStatus(List<Long> optionIds, Long storeId) {
        return catalogQueryService.findOptionStoreStatus(optionIds, storeId);
    }
}
