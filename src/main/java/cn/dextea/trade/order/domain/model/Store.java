package cn.dextea.trade.order.domain.model;

import cn.dextea.trade.order.domain.exception.OrderErrorCode;
import cn.dextea.trade.order.domain.model.enumeration.StoreStatus;
import cn.dextea.trade.shared.domain.error.BizError;

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
    private StoreStatus status;

    public boolean isActive() {
        return status == StoreStatus.OPEN;
    }

    public void ensureActive() {
        if (!isActive()) {
            throw new BizError(OrderErrorCode.STORE_INACTIVE);
        }
    }
}
