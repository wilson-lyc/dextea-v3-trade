package cn.dextea.trade.order.domain.model;

import cn.dextea.trade.order.domain.model.enums.CustomerStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Customer {
    private Long id;
    private CustomerStatus status;

    public boolean isActive() {
        return status == CustomerStatus.ACTIVE;
    }
}
