package cn.dextea.trade.order.infrastructure.adapter;

import cn.dextea.trade.catalog.domain.model.Customization;
import cn.dextea.trade.catalog.domain.model.CustomizationOption;
import cn.dextea.trade.catalog.domain.model.CustomizationOptionStoreStatus;
import cn.dextea.trade.catalog.domain.model.Customer;
import cn.dextea.trade.catalog.domain.model.Gallery;
import cn.dextea.trade.catalog.domain.model.Product;
import cn.dextea.trade.catalog.domain.model.ProductImage;
import cn.dextea.trade.catalog.domain.model.ProductStoreStatus;
import cn.dextea.trade.catalog.domain.model.Store;
import cn.dextea.trade.catalog.domain.repository.CatalogRepository;
import cn.dextea.trade.order.domain.port.CatalogPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 商品目录防腐适配器：实现订单领域 {@link CatalogPort}，委托 catalog 仓储获取只读快照。
 */
@Component
@RequiredArgsConstructor
public class CatalogAdapter implements CatalogPort {

    private final CatalogRepository catalogRepository;

    @Override
    public List<Product> findProducts(List<Long> ids) {
        return catalogRepository.findProductsByIds(ids);
    }

    @Override
    public List<ProductImage> findCoverImages(List<Long> productIds) {
        return catalogRepository.findCoverImagesByProductIds(productIds);
    }

    @Override
    public List<Gallery> findGalleries(List<Long> imageIds) {
        return catalogRepository.findGalleriesByIds(imageIds);
    }

    @Override
    public List<ProductStoreStatus> findProductStoreStatus(List<Long> productIds, Long storeId) {
        return catalogRepository.findProductStoreStatus(productIds, storeId);
    }

    @Override
    public List<Customization> findCustomizations(List<Long> ids) {
        return catalogRepository.findCustomizationsByIds(ids);
    }

    @Override
    public List<CustomizationOption> findOptions(List<Long> ids) {
        return catalogRepository.findCustomizationOptionsByIds(ids);
    }

    @Override
    public List<CustomizationOptionStoreStatus> findOptionStoreStatus(List<Long> optionIds, Long storeId) {
        return catalogRepository.findOptionStoreStatus(optionIds, storeId);
    }

    @Override
    public Store findStore(Long id) {
        return catalogRepository.findStoreById(id);
    }

    @Override
    public Customer findCustomer(Long id) {
        return catalogRepository.findCustomerById(id);
    }
}
