package cn.dextea.trade.order.domain.service;

import java.util.List;
import java.util.Set;

public interface SkuIdService {
    Long extractProductId(String skuId);

    Set<Long> extractProductIds(List<String> skuIds);
}
