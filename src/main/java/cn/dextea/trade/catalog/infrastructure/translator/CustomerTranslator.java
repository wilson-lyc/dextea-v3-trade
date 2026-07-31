package cn.dextea.trade.catalog.infrastructure.translator;

import cn.dextea.trade.catalog.domain.model.aggregate.Customer;
import cn.dextea.trade.catalog.infrastructure.po.CustomerPO;

public final class CustomerTranslator {
    private CustomerTranslator() {
    }

    public static Customer toCustomer(CustomerPO po) {
        if (po == null) {
            return null;
        }
        return Customer.builder()
                .id(po.getId())
                .name(po.getName())
                .status(po.getStatus())
                .alipayOpenId(po.getAlipayOpenId())
                .build();
    }
}
