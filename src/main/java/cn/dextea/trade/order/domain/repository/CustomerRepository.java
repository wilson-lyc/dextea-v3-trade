package cn.dextea.trade.order.domain.repository;

import cn.dextea.trade.order.domain.model.Customer;

public interface CustomerRepository {
    Customer getCustomerById(Long id);
}
