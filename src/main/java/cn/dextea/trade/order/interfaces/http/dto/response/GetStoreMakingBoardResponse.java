package cn.dextea.trade.order.interfaces.http.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetStoreMakingBoardResponse {

    private List<String> preparingPickupCodes;

    private List<String> readyPickupCodes;

    private Long preparingOrderCount;

    private Integer preparingProductQuantity;
}
