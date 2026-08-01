package cn.dextea.trade.order.domain.model.valueobject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreBuildProductInput {
    private String skuId;
    private Integer quantity;
}
