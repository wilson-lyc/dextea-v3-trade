package cn.dextea.trade.order.domain.model;

import cn.dextea.trade.shared.model.Quantity;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SkuItem {

    private final String skuId;
    private final Quantity quantity;
}
