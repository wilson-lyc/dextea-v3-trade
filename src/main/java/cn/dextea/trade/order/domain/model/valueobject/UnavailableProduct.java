package cn.dextea.trade.order.domain.model.valueobject;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UnavailableProduct {
    private Long id;
    private String name;
}
