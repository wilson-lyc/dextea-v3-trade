package cn.dextea.trade.order.application.facade;

import cn.dextea.trade.order.domain.gateway.CustomerGateway;
import cn.dextea.trade.order.domain.gateway.CustomizationGateway;
import cn.dextea.trade.order.domain.gateway.ProductGateway;
import cn.dextea.trade.order.domain.gateway.StoreGateway;
import cn.dextea.trade.order.domain.model.valueobject.Customer;
import cn.dextea.trade.order.domain.model.valueobject.CustomizationOption;
import cn.dextea.trade.order.domain.model.valueobject.Store;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 外部数据门面：统一聚合商品/客制化/门店/顾客四个领域网关的只读调用，
 * 避免应用服务散落地直接依赖多个网关。
 */
@Component
@RequiredArgsConstructor
public class ExternalDataFacade {

    private final ProductGateway productGateway;
    private final CustomizationGateway customizationGateway;
    private final StoreGateway storeGateway;
    private final CustomerGateway customerGateway;

    /** 查询顾客只读快照（支付绑卡、可用性校验用）。 */
    public Customer findCustomer(Long customerId) {
        return customerGateway.findCustomer(customerId);
    }

    /** 查询门店只读快照（列表/详情展示用）。 */
    public Store findStore(Long storeId) {
        return storeGateway.findStore(storeId);
    }

    /** 批量将封面标识还原为封面 URL（coverId → url）。 */
    public Map<Long, String> findCoverUrls(List<Long> coverIds) {
        return productGateway.findCoverUrls(coverIds);
    }

    /** 批量查询客制化选项只读快照（还原客制化文本用）。 */
    public List<CustomizationOption> findOptions(List<Long> optionIds) {
        return customizationGateway.findOptions(optionIds);
    }
}
