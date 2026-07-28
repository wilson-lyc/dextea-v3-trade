package cn.dextea.trade.order.domain.gateway;
import cn.dextea.trade.order.domain.model.valueobject.Customer;
public interface CustomerGateway {
    Customer findCustomer(Long id);
}
