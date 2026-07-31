package cn.dextea.trade.order.domain.model.valueobject;
import cn.dextea.trade.catalog.domain.model.valueobject.UnavailableCustomization;
import cn.dextea.trade.catalog.domain.model.valueobject.UnavailableProduct;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;
import java.math.BigDecimal;
import java.util.List;
@Getter
@Builder
@Jacksonized
public class PreBuildResult {
    private List<UnavailableProduct> unavailableProducts;
    private List<UnavailableCustomization> unavailableCustomizations;
    private List<PricedOrderItem> products;
    private int totalQuantity;
    private BigDecimal totalPrice;
    public boolean hasUnavailable() {
        boolean products = unavailableProducts != null && !unavailableProducts.isEmpty();
        boolean customization = unavailableCustomizations != null && !unavailableCustomizations.isEmpty();
        return products || customization;
    }
}
