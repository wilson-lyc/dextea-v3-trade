package cn.dextea.trade.order.infrastructure.persistence.repository;

import cn.dextea.trade.order.domain.model.Customer;
import cn.dextea.trade.order.domain.repository.CustomerRepository;
import cn.dextea.trade.order.infrastructure.persistence.converter.CustomerConverter;
import cn.dextea.trade.order.infrastructure.persistence.mapper.CustomerMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CustomerRepositoryImpl implements CustomerRepository {

    private final CustomerMapper customerMapper;
    private final CustomerConverter customerConverter;

    @Override
    public Customer getCustomerById(Long id) {
        return customerConverter.toDomain(customerMapper.selectById(id));
    }
}
