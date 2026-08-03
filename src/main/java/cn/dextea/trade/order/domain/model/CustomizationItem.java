package cn.dextea.trade.order.domain.model;

import cn.dextea.trade.order.domain.model.enumeration.CustomizationItemStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomizationItem {
    private Long id;
    private String name;
    private CustomizationItemStatus status;
    private List<CustomizationOption> options;

    public boolean isActive() {
        return status == CustomizationItemStatus.ACTIVE;
    }
}
