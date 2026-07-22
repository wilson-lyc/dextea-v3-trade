package cn.dextea.trade.util;

import cn.dextea.trade.exception.BizError;
import cn.dextea.trade.error.OrderErrorCode;

import java.util.ArrayList;
import java.util.List;

/**
 * skuId 解析工具
 *
 * skuId 格式：商品ID#客制化项目ID_客制化选项ID-客制化项目ID_客制化选项ID...
 */
public final class SkuIdParser {

    private SkuIdParser() {
    }

    /**
     * 提取商品ID
     *
     * @param skuId SKU 标识
     * @return 商品ID
     */
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

    /**
     * 提取客制化项目ID列表
     *
     * @param skuId SKU 标识
     * @return 客制化项目ID列表
     */
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

    /**
     * 提取客制化选项ID列表
     *
     * @param skuId SKU 标识
     * @return 客制化选项ID列表
     */
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
    
    /**
     * 字符串转长整型
     *
     * @param value 待转换字符串
     * @param skuId SKU 标识
     * @return 长整型数值
     */
    private static long parseLong(String value, String skuId) {
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            throw new BizError(OrderErrorCode.SKU_INVALID, "skuId 格式非法: " + skuId);
        }
    }
}
