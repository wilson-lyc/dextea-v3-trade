package cn.dextea.trade.order.domain.gateway;

import cn.dextea.trade.order.domain.model.valueobject.Customer;

/**
 * 顾客网关：提供顾客只读快照，用于下单前顾客可用性校验与支付绑卡。
 */
public interface CustomerGateway {

    Customer findCustomer(Long id);
}
