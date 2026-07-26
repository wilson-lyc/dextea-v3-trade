package cn.dextea.trade.infrastructure.acl.customer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CustomerQueryImpl implements CustomerQuery {

    private final CustomerMapper customerMapper;

    @Override
    public CustomerPO findById(Long id) {
        if (id == null) {
            return null;
        }
        return customerMapper.selectById(id);
    }

    @Override
    public List<CustomerPO> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return customerMapper.selectByIds(ids);
    }
}
