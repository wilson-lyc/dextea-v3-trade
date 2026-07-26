package cn.dextea.trade.order.infrastructure.adapter;

import cn.dextea.trade.catalog.domain.model.Customer;
import cn.dextea.trade.catalog.domain.service.CatalogQueryService;
import cn.dextea.trade.order.domain.port.CustomerPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 顾客防腐适配器：实现订单领域 {@link CustomerPort}，委托 catalog 查询服务。
 */
@Component
@RequiredArgsConstructor
public class CustomerAdapter implements CustomerPort {

    private final CatalogQueryService catalogQueryService;

    @Override
    public Customer findById(Long id) {
        return catalogQueryService.findCustomerById(id);
    }
}
