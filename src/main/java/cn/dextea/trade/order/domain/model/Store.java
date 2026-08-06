package cn.dextea.trade.order.domain.model;

import cn.dextea.trade.order.domain.model.enumeration.StoreStatus;
import cn.dextea.trade.shared.error.Activatable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Store implements Activatable {
    private Long id;
    private String name;
    private StoreStatus status;

    @Override
    public boolean isActive() {
        return status == StoreStatus.OPEN;
    }
}
