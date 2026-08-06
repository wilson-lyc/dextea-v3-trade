package cn.dextea.trade.order.domain.model;

import cn.dextea.trade.order.domain.model.enumeration.CustomerStatus;
import cn.dextea.trade.shared.error.Activatable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Customer implements Activatable {
    private Long id;
    private String weixinOpenId;
    private String alipayOpenId;
    private CustomerStatus status;

    @Override
    public boolean isActive() {
        return status == CustomerStatus.ACTIVE;
    }
}
