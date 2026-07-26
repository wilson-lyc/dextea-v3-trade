package cn.dextea.trade.infrastructure.acl.customer;

import java.util.List;

public interface CustomerQuery {

    CustomerPO findById(Long id);

    List<CustomerPO> findByIds(List<Long> ids);
}
