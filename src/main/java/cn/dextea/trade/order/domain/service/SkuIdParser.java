package cn.dextea.trade.order.domain.service;

import cn.dextea.trade.order.domain.exception.OrderErrorCode;
import cn.dextea.trade.shared.error.BizError;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

@Component
public class SkuIdParser {

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

    public List<long[]> parseCustomizationPairs(String skuId) {
        List<long[]> pairs = new ArrayList<>();
        if (skuId == null || !skuId.contains("#")) {
            return pairs;
        }
        String specPart = skuId.substring(skuId.indexOf('#') + 1);
        if (specPart.isEmpty()) {
            return pairs;
        }
        for (String pair : specPart.split("-")) {
            String[] ids = pair.split("_");
            if (ids.length != 2) {
                throw new BizError(OrderErrorCode.INVALID_SKU);
            }
            pairs.add(new long[]{parseSkuIdPart(ids[0]), parseSkuIdPart(ids[1])});
        }
        pairs.sort((a, b) -> Long.compare(a[0], b[0]));
        return pairs;
    }

    private long parseSkuIdPart(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new BizError(OrderErrorCode.INVALID_SKU);
        }
    }
}
