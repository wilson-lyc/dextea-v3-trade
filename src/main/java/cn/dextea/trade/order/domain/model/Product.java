package cn.dextea.trade.order.domain.model;

import cn.dextea.trade.order.domain.model.enums.ProductGlobalStatus;
import cn.dextea.trade.order.domain.model.enums.ProductStoreStatus;
import cn.dextea.trade.shared.domain.model.Money;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    private Long id;
    private String name;
    private ProductGlobalStatus globalStatus;
    private ProductStoreStatus storeStatus;
    private Money price;
    private Long coverId;
    private List<CustomizationItem> customization;

    public boolean isActive() {
        return globalStatus == ProductGlobalStatus.ACTIVE
                && storeStatus == ProductStoreStatus.ACTIVE;
    }
}
