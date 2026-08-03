package cn.dextea.trade.order.domain.model;

import cn.dextea.trade.order.domain.exception.OrderErrorCode;
import cn.dextea.trade.order.domain.model.enumeration.StoreStatus;
import cn.dextea.trade.shared.domain.error.BizError;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
            log.warn("门店不可下单, storeId={}, status={}", id, status);
            throw new BizError(OrderErrorCode.STORE_INACTIVE);
        }
    }
}
