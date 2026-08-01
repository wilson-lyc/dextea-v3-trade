package cn.dextea.trade.order.domain.model.valueobject;

import cn.dextea.trade.order.domain.model.aggregate.Customer;
import cn.dextea.trade.order.domain.model.aggregate.Product;
import cn.dextea.trade.order.domain.model.aggregate.Store;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class CatalogSnapshot {

    private final Store store;
    private final Customer customer;
    private final Map<Long, Product> products;
    private final Map<Long, ProductCover> productCovers;
    private final Map<Long, ProductStoreStatus> productStoreStatuses;
    private final Map<Long, Customization> customizations;
    private final Map<Long, CustomizationOption> options;
    private final Map<Long, CustomizationOptionStoreStatus> optionStoreStatuses;

    public Product product(Long productId) {
        return products == null ? null : products.get(productId);
    }

    public ProductCover cover(Long productId) {
        return productCovers == null ? null : productCovers.get(productId);
    }

    public ProductStoreStatus productStoreStatus(Long productId) {
        return productStoreStatuses == null ? null : productStoreStatuses.get(productId);
    }

    public Customization customization(Long customizationId) {
        return customizations == null ? null : customizations.get(customizationId);
    }

    public CustomizationOption option(Long optionId) {
        return options == null ? null : options.get(optionId);
    }

    public CustomizationOptionStoreStatus optionStoreStatus(Long optionId) {
        return optionStoreStatuses == null ? null : optionStoreStatuses.get(optionId);
    }
}
