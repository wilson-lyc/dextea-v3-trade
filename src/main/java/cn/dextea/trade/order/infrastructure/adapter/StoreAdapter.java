package cn.dextea.trade.order.infrastructure.adapter;

import cn.dextea.trade.catalog.domain.model.Store;
import cn.dextea.trade.catalog.domain.service.CatalogQueryService;
import cn.dextea.trade.order.domain.port.StorePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 门店防腐适配器：实现订单领域 {@link StorePort}，委托 catalog 查询服务。
 */
@Component
@RequiredArgsConstructor
public class StoreAdapter implements StorePort {

    private final CatalogQueryService catalogQueryService;

    @Override
    public Store findById(Long id) {
        return catalogQueryService.findStoreById(id);
    }
}
