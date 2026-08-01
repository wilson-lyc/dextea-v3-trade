package cn.dextea.trade.order.domain.port;

import cn.dextea.trade.order.domain.model.aggregate.Customer;

public interface CustomerRepository {

    Customer findCustomer(Long id);
}
