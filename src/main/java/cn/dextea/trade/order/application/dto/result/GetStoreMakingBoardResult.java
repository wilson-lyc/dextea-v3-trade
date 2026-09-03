package cn.dextea.trade.order.application.dto.result;

import cn.dextea.trade.shared.model.Quantity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetStoreMakingBoardResult {

    private List<String> preparingPickupCodes;

    private List<String> readyPickupCodes;

    private Long preparingOrderCount;

    private Quantity preparingProductQuantity;
}
