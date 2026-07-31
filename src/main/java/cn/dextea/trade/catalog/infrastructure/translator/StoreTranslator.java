package cn.dextea.trade.catalog.infrastructure.translator;

import cn.dextea.trade.catalog.domain.model.aggregate.Store;
import cn.dextea.trade.catalog.infrastructure.po.StorePO;

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
