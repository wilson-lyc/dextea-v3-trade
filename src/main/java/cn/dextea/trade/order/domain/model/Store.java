package cn.dextea.trade.order.domain.model;

import cn.dextea.trade.order.domain.model.enums.StoreStatus;

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
}
