package cn.dextea.trade.order.infrastructure.persistence.converter;

import cn.dextea.trade.order.domain.model.Store;
import cn.dextea.trade.order.domain.model.enums.StoreStatus;
import cn.dextea.trade.order.infrastructure.persistence.po.StorePO;
import cn.dextea.trade.shared.domain.enumeration.EnumUtils;
import org.springframework.stereotype.Component;

@Component
public class StoreConverter {

    public Store toDomain(StorePO po) {
        if (po == null) {
            return null;
        }
        return Store.builder()
                .id(po.getId())
                .status(EnumUtils.of(StoreStatus.class, po.getStatus()))
                .build();
    }

    public StorePO toPO(Store store) {
        if (store == null) {
            return null;
        }
        StorePO po = new StorePO();
        po.setId(store.getId());
        po.setStatus(store.getStatus() == null ? null : store.getStatus().getCode());
        return po;
    }
}
