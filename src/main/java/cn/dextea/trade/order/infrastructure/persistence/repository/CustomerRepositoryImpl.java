package cn.dextea.trade.order.infrastructure.persistence.repository;

import cn.dextea.trade.order.domain.exception.OrderErrorCode;
import cn.dextea.trade.order.domain.model.Customer;
import cn.dextea.trade.order.domain.repository.CustomerRepository;
import cn.dextea.trade.order.infrastructure.persistence.converter.CustomerConverter;
import cn.dextea.trade.order.infrastructure.persistence.mapper.CustomerMapper;
import cn.dextea.trade.order.infrastructure.persistence.po.CustomerPO;
import cn.dextea.trade.shared.domain.error.BizError;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CustomerRepositoryImpl implements CustomerRepository {

    private final CustomerMapper customerMapper;
    private final CustomerConverter customerConverter;

    @Override
    public Customer getCustomerById(Long id) {
        CustomerPO po = customerMapper.selectById(id);
        if (po == null) {
            throw new BizError(OrderErrorCode.CUSTOMER_NOT_FOUND);
        }
        return customerConverter.toDomain(po);
    }
}
