package cn.dextea.trade.order.domain.model.valueobject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreBuildContext {
    private Long storeId;
    private Long customerId;
    private List<PreBuildProductInput> products;
}
