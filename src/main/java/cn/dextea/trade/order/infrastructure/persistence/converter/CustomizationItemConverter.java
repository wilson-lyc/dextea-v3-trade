package cn.dextea.trade.order.infrastructure.persistence.converter;

import cn.dextea.trade.order.domain.model.CustomizationItem;
import cn.dextea.trade.order.domain.model.CustomizationOption;
import cn.dextea.trade.order.domain.enumeration.CustomizationItemStatus;
import cn.dextea.trade.order.infrastructure.persistence.po.CustomizationItemPO;
import cn.dextea.trade.shared.enumeration.EnumUtils;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CustomizationItemConverter {

    public CustomizationItem toDomain(CustomizationItemPO po, List<CustomizationOption> options) {
        return CustomizationItem.builder()
                .id(po.getId())
                .name(po.getName())
                .status(EnumUtils.of(CustomizationItemStatus.class, po.getStatus()))
                .options(options)
                .build();
    }
}
