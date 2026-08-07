package cn.dextea.trade.order.domain.model;

import cn.dextea.trade.order.domain.exception.OrderErrorCode;
import cn.dextea.trade.order.domain.enumeration.StoreStatus;
import cn.dextea.trade.shared.error.BizError;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Store {
    private Long id;
    private String name;
    private StoreStatus status;

    public void ensureActive() {
        if (status != StoreStatus.OPEN) {
            throw new BizError(OrderErrorCode.STORE_INACTIVE);
        }
    }
}
