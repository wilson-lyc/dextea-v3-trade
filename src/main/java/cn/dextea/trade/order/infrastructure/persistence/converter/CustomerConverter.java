package cn.dextea.trade.order.infrastructure.persistence.converter;

import cn.dextea.trade.order.domain.model.Customer;
import cn.dextea.trade.order.domain.model.enumeration.CustomerStatus;
import cn.dextea.trade.order.infrastructure.persistence.po.CustomerPO;
import cn.dextea.trade.shared.domain.enumeration.EnumUtils;
import org.springframework.stereotype.Component;

@Component
public class CustomerConverter {

    public Customer toDomain(CustomerPO po) {
        if (po == null) {
            return null;
        }
        return Customer.builder()
                .id(po.getId())
                .weixinOpenId(po.getWeixinOpenId())
                .alipayOpenId(po.getAlipayOpenId())
                .status(EnumUtils.of(CustomerStatus.class, po.getStatus()))
                .build();
    }

    public CustomerPO toPO(Customer customer) {
        if (customer == null) {
            return null;
        }
        CustomerPO po = new CustomerPO();
        po.setId(customer.getId());
        po.setStatus(customer.getStatus() == null ? null : customer.getStatus().getCode());
        return po;
    }
}
