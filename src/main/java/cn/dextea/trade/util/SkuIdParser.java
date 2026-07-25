package cn.dextea.trade.util;

import cn.dextea.trade.exception.BizError;
import cn.dextea.trade.errorcode.OrderErrorCode;

import java.util.ArrayList;
import java.util.List;

public final class SkuIdParser {

    private SkuIdParser() {
    }

    public static Long parseProductId(String skuId) {
        if (skuId == null || skuId.isBlank()) {
            throw new BizError(OrderErrorCode.SKU_INVALID, "skuId 不能为空");
        }
        int hashIndex = skuId.indexOf('#');
        if (hashIndex <= 0) {
            throw new BizError(OrderErrorCode.SKU_INVALID, "skuId 格式非法: " + skuId);
        }
        return parseLong(skuId.substring(0, hashIndex), skuId);
    }

    public static List<Long> parseItemIds(String skuId) {
        if (skuId == null || skuId.isBlank()) {
            throw new BizError(OrderErrorCode.SKU_INVALID, "skuId 不能为空");
        }
        int hashIndex = skuId.indexOf('#');
        if (hashIndex <= 0) {
            throw new BizError(OrderErrorCode.SKU_INVALID, "skuId 格式非法: " + skuId);
        }
        String customizationPart = skuId.substring(hashIndex + 1);
        List<Long> itemIds = new ArrayList<>();
        if (customizationPart.isBlank()) {
            return itemIds;
        }

        String[] pairs = customizationPart.split("-");
        for (String pair : pairs) {
            if (pair.isBlank()) {
                throw new BizError(OrderErrorCode.SKU_INVALID, "skuId 格式非法: " + skuId);
            }
            String[] kv = pair.split("_");
            if (kv.length != 2 || kv[0].isBlank() || kv[1].isBlank()) {
                throw new BizError(OrderErrorCode.SKU_INVALID, "skuId 格式非法: " + skuId);
            }
            itemIds.add(parseLong(kv[0], skuId));
        }
        return itemIds;
    }

    public static List<Long> parseOptionIds(String skuId) {
        if (skuId == null || skuId.isBlank()) {
            throw new BizError(OrderErrorCode.SKU_INVALID, "skuId 不能为空");
        }
        int hashIndex = skuId.indexOf('#');
        if (hashIndex <= 0) {
            throw new BizError(OrderErrorCode.SKU_INVALID, "skuId 格式非法: " + skuId);
        }
        String customizationPart = skuId.substring(hashIndex + 1);
        List<Long> optionIds = new ArrayList<>();
        if (customizationPart.isBlank()) {
            return optionIds;
        }

        String[] pairs = customizationPart.split("-");
        for (String pair : pairs) {
            if (pair.isBlank()) {
                throw new BizError(OrderErrorCode.SKU_INVALID, "skuId 格式非法: " + skuId);
            }
            String[] kv = pair.split("_");
            if (kv.length != 2 || kv[0].isBlank() || kv[1].isBlank()) {
                throw new BizError(OrderErrorCode.SKU_INVALID, "skuId 格式非法: " + skuId);
            }
            optionIds.add(parseLong(kv[1], skuId));
        }
        return optionIds;
    }
    
    private static long parseLong(String value, String skuId) {
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            throw new BizError(OrderErrorCode.SKU_INVALID, "skuId 格式非法: " + skuId);
        }
    }
}
