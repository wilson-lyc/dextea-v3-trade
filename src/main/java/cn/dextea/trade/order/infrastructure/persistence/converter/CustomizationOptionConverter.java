package cn.dextea.trade.order.infrastructure.persistence.converter;

import cn.dextea.trade.order.domain.model.CustomizationOption;
import cn.dextea.trade.order.domain.enumeration.CustomizationOptionGlobalStatus;
import cn.dextea.trade.order.domain.enumeration.CustomizationOptionStoreStatus;
import cn.dextea.trade.order.infrastructure.persistence.po.CustomizationOptionPO;
import cn.dextea.trade.order.infrastructure.persistence.po.CustomizationOptionStoreStatusPO;
import cn.dextea.trade.shared.enumeration.EnumUtils;
import cn.dextea.trade.shared.model.Money;
import org.springframework.stereotype.Component;

@Component
public class CustomizationOptionConverter {

    public CustomizationOption toDomain(CustomizationOptionPO po,
                                        CustomizationOptionStoreStatusPO storeStatusPO) {
        return CustomizationOption.builder()
                .id(po.getId())
                .name(po.getName())
                .price(Money.of(po.getPrice()))
                .globalStatus(EnumUtils.of(CustomizationOptionGlobalStatus.class, po.getStatus()))
                .storeStatus(storeStatusOrDefault(storeStatusPO))
                .build();
    }

    public CustomizationOptionStoreStatus storeStatusOrDefault(
            CustomizationOptionStoreStatusPO storeStatusPO) {
        if (storeStatusPO == null) {
            return CustomizationOptionStoreStatus.DISABLED;
        }
        return EnumUtils.of(CustomizationOptionStoreStatus.class, storeStatusPO.getStatus());
    }
}
