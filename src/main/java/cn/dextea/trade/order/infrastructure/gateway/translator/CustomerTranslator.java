package cn.dextea.trade.order.infrastructure.gateway.translator;

import cn.dextea.trade.order.domain.model.valueobject.Customer;
import cn.dextea.trade.order.infrastructure.gateway.po.CustomerPO;

/**
 * 顾客 PO → 领域值对象清洗器。
 */
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
