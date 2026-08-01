package cn.dextea.trade.order.infrastructure.persistence.translator;

import cn.dextea.trade.order.domain.model.aggregate.Store;
import cn.dextea.trade.order.infrastructure.persistence.po.StorePO;

public final class StoreTranslator {
    private StoreTranslator() {
    }

    public static Store toStore(StorePO po) {
        if (po == null) {
            return null;
        }
        return Store.builder()
                .id(po.getId())
                .name(po.getName())
                .status(po.getStatus())
                .address(po.getAddress())
                .phone(po.getPhone())
                .businessHours(po.getBusinessHours())
                .build();
    }
}
