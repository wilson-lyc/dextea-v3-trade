package cn.dextea.trade.order.domain.model;

import cn.dextea.trade.order.domain.model.enums.CustomizationOptionGlobalStatus;
import cn.dextea.trade.order.domain.model.enums.CustomizationOptionStoreStatus;
import cn.dextea.trade.shared.domain.money.Money;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomizationOption {
    private Long id;
    private String name;
    private Money price;
    private CustomizationOptionGlobalStatus globalStatus;
    private CustomizationOptionStoreStatus storeStatus;

    public boolean isActive() {
        return globalStatus == CustomizationOptionGlobalStatus.ACTIVE
                && storeStatus == CustomizationOptionStoreStatus.ACTIVE;
    }
}
