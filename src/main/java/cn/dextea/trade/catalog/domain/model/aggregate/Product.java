package cn.dextea.trade.catalog.domain.model.aggregate;

import cn.dextea.trade.catalog.domain.exception.CatalogErrorCode;
import cn.dextea.trade.catalog.domain.exception.CatalogException;
import cn.dextea.trade.catalog.domain.model.valueobject.Customization;
import cn.dextea.trade.catalog.domain.model.valueobject.CustomizationOption;
import cn.dextea.trade.catalog.domain.model.valueobject.CustomizationOptionStoreStatus;
import cn.dextea.trade.catalog.domain.model.valueobject.ProductCover;
import cn.dextea.trade.catalog.domain.model.valueobject.ProductGlobalStatus;
import cn.dextea.trade.catalog.domain.model.valueobject.ProductStoreStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import lombok.Builder.Default;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    private Long id;
    private String name;
    private String brief;
    private String description;
    private Integer status;
    private BigDecimal price;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private ProductCover cover;
    @Default
    private List<Customization> customizations = new ArrayList<>();
    @Default
    private List<CustomizationOption> options = new ArrayList<>();
    @Default
    private List<ProductStoreStatus> storeStatuses = new ArrayList<>();

    public Optional<ProductGlobalStatus> getGlobalStatus() {
        return status == null ? Optional.empty() : Optional.of(ProductGlobalStatus.of(status));
    }

    public boolean isOnShelf() {
        return status != null && ProductGlobalStatus.ON_SHELF.getCode() == status;
    }

    public boolean isAvailableInStore(ProductStoreStatus storeStatus) {
        return isOnShelf() && storeStatus != null && storeStatus.isAvailable();
    }

    public Optional<ProductStoreStatus> storeStatusOf(Long storeId) {
        return storeStatuses.stream()
                .filter(s -> s.getStoreId() != null && s.getStoreId().equals(storeId))
                .findFirst();
    }

    public boolean isCustomizationBelongToProduct(Long customizationId) {
        return customizationId != null && customizations.stream()
                .anyMatch(c -> c.getId() != null && c.getId().equals(customizationId));
    }

    public boolean isOptionBelongToCustomization(Long customizationId, Long optionId) {
        return customizationId != null && optionId != null && options.stream()
                .anyMatch(o -> o.getId() != null && o.getId().equals(optionId)
                        && o.getCustomizationId() != null && o.getCustomizationId().equals(customizationId));
    }

    public boolean isOptionAvailableInStore(CustomizationOption option, CustomizationOptionStoreStatus storeStatus) {
        return option != null && option.isAvailableInStore(storeStatus);
    }

    public void assertCustomizationBinding(Long customizationId, Long optionId) {
        if (!isCustomizationBelongToProduct(customizationId)) {
            throw new CatalogException(CatalogErrorCode.CUSTOMIZATION_BINDING_INVALID,
                    "客制化项目不属于该商品: productId=" + id + ", customizationId=" + customizationId);
        }
        if (!isOptionBelongToCustomization(customizationId, optionId)) {
            throw new CatalogException(CatalogErrorCode.CUSTOMIZATION_BINDING_INVALID,
                    "客制化选项不属于该项目: productId=" + id
                            + ", customizationId=" + customizationId + ", optionId=" + optionId);
        }
    }
}
