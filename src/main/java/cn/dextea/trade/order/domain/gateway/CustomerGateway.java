package cn.dextea.trade.order.domain.gateway;
import cn.dextea.trade.catalog.domain.model.aggregate.Customer;
public interface CustomerGateway {
    Customer findCustomer(Long id);
}
