package cn.dextea.trade.order.infrastructure.persistence.repository;

import cn.dextea.trade.order.domain.model.aggregate.Customer;
import cn.dextea.trade.order.domain.port.CustomerRepository;
import cn.dextea.trade.order.infrastructure.persistence.mapper.ProductMapper;
import cn.dextea.trade.order.infrastructure.persistence.translator.CustomerTranslator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CustomerRepositoryImpl implements CustomerRepository {

    private final ProductMapper productMapper;

    @Override
    public Customer findCustomer(Long id) {
        if (id == null) {
            return null;
        }
        return CustomerTranslator.toCustomer(productMapper.selectCustomerById(id));
    }
}
