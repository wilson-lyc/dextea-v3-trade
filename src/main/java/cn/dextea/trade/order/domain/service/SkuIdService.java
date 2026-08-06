package cn.dextea.trade.order.domain.service;

import cn.dextea.trade.order.domain.exception.OrderErrorCode;
import cn.dextea.trade.shared.error.BizError;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class SkuIdService {

    public Long extractProductId(String skuId) {
        if (skuId == null || skuId.isEmpty()) {
            throw new BizError(OrderErrorCode.INVALID_SKU, "SKU 不能为空");
        }
        String prefix = skuId.contains("#") ? skuId.substring(0, skuId.indexOf('#')) : skuId;
        if (prefix.isEmpty() || !prefix.chars().allMatch(Character::isDigit)) {
            throw new BizError(OrderErrorCode.INVALID_SKU, "非法的SKU前缀: " + skuId);
        }
        return Long.parseLong(prefix);
    }

    public Set<Long> extractProductIds(List<String> skuIds) {
        Set<Long> productIds = new HashSet<>();
        for (String skuId : skuIds) {
            if (skuId == null || skuId.isEmpty()) {
                continue;
            }
            productIds.add(extractProductId(skuId));
        }
        return productIds;
    }
}
