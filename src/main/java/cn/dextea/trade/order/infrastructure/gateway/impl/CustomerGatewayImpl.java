package cn.dextea.trade.order.infrastructure.gateway.impl;

import cn.dextea.trade.order.domain.gateway.CustomerGateway;
import cn.dextea.trade.order.domain.model.valueobject.Customer;
import cn.dextea.trade.order.infrastructure.gateway.mapper.CatalogMapper;
import cn.dextea.trade.order.infrastructure.gateway.translator.CustomerTranslator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 顾客网关实现（ACL）：查询外部顾客表并清洗为领域值对象。
 */
@Component
@RequiredArgsConstructor
public class CustomerGatewayImpl implements CustomerGateway {

    private final CatalogMapper catalogMapper;

    @Override
    public Customer findCustomer(Long id) {
        if (id == null) {
            return null;
        }
        return CustomerTranslator.toCustomer(catalogMapper.selectCustomerById(id));
    }
}
