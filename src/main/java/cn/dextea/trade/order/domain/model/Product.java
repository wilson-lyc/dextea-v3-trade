package cn.dextea.trade.order.domain.model;

import cn.dextea.trade.order.domain.exception.OrderErrorCode;
import cn.dextea.trade.order.domain.model.enumeration.ProductGlobalStatus;
import cn.dextea.trade.order.domain.model.enumeration.ProductStoreStatus;
import cn.dextea.trade.shared.domain.error.BizError;
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
    private ProductCover cover;
    private List<CustomizationItem> customization;

    public boolean isActive() {
        return globalStatus == ProductGlobalStatus.ACTIVE
                && storeStatus == ProductStoreStatus.ACTIVE;
    }
}
