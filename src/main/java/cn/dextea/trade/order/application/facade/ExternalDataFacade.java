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
@Component
@RequiredArgsConstructor
public class ExternalDataFacade {
    private final ProductGateway productGateway;
    private final CustomizationGateway customizationGateway;
    private final StoreGateway storeGateway;
    private final CustomerGateway customerGateway;
    public Customer findCustomer(Long customerId) {
        return customerGateway.findCustomer(customerId);
    }
    public Store findStore(Long storeId) {
        return storeGateway.findStore(storeId);
    }
    public Map<Long, String> findCoverUrls(List<Long> coverIds) {
        return productGateway.findCoverUrls(coverIds);
    }
    public List<CustomizationOption> findOptions(List<Long> optionIds) {
        return customizationGateway.findOptions(optionIds);
    }
}
