package cn.dextea.trade.order.infrastructure.persistence.translator;

import cn.dextea.trade.order.domain.model.valueobject.Customization;
import cn.dextea.trade.order.domain.model.valueobject.CustomizationOption;
import cn.dextea.trade.order.domain.model.valueobject.CustomizationOptionStoreStatus;
import cn.dextea.trade.order.infrastructure.persistence.po.CustomizationOptionPO;
import cn.dextea.trade.order.infrastructure.persistence.po.CustomizationOptionStoreStatusPO;
import cn.dextea.trade.order.infrastructure.persistence.po.CustomizationPO;

import java.util.List;
import java.util.stream.Collectors;

public final class CustomizationTranslator {
    private CustomizationTranslator() {
    }

    public static Customization toCustomization(CustomizationPO po) {
        if (po == null) {
            return null;
        }
        return Customization.builder()
                .id(po.getId())
                .productId(po.getProductId())
                .name(po.getName())
                .status(po.getStatus())
                .build();
    }

    public static List<Customization> toCustomizations(List<CustomizationPO> pos) {
        return pos == null ? List.of()
                : pos.stream().map(CustomizationTranslator::toCustomization).collect(Collectors.toList());
    }

    public static CustomizationOption toOption(CustomizationOptionPO po) {
        if (po == null) {
            return null;
        }
        return CustomizationOption.builder()
                .id(po.getId())
                .customizationId(po.getCustomizationId())
                .name(po.getName())
                .price(po.getPrice())
                .status(po.getStatus())
                .build();
    }

    public static List<CustomizationOption> toOptions(List<CustomizationOptionPO> pos) {
        return pos == null ? List.of()
                : pos.stream().map(CustomizationTranslator::toOption).collect(Collectors.toList());
    }

    public static CustomizationOptionStoreStatus toOptionStoreStatus(CustomizationOptionStoreStatusPO po) {
        if (po == null) {
            return null;
        }
        return CustomizationOptionStoreStatus.builder()
                .customizationOptionId(po.getCustomizationOptionId())
                .storeId(po.getStoreId())
                .status(po.getStatus())
                .build();
    }

    public static List<CustomizationOptionStoreStatus> toOptionStoreStatusList(List<CustomizationOptionStoreStatusPO> pos) {
        return pos == null ? List.of()
                : pos.stream().map(CustomizationTranslator::toOptionStoreStatus).collect(Collectors.toList());
    }
}
