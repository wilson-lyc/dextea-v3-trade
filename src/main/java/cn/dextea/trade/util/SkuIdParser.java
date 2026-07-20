package cn.dextea.trade.util;

import cn.dextea.trade.common.BizError;
import cn.dextea.trade.error.OrderErrorCode;

import java.util.ArrayList;
import java.util.List;

/**
 * skuId 解析工具。
 *
 * <p>skuId 格式：{@code 商品ID#客制化项目ID_客制化选项ID-客制化项目ID_客制化选项ID...}。</p>
 * <p>例如 {@code 1#1_1} 表示：商品ID=1，客制化项目ID=1，客制化选项ID=1。</p>
 * <p>{@code #} 之后为客制化部分，可为空（无客制化）；多个「项目_选项」对以 {@code -} 分隔，
 * 每对以 {@code _} 分隔，本接口仅需提取「客制化选项ID」。</p>
 */
public final class SkuIdParser {

    private SkuIdParser() {
    }

    /**
     * 从 skuId 中提取客制化选项ID列表。格式非法时抛出 {@link BizError}。
     *
     * @param skuId SKU 标识
     * @return 客制化选项ID列表；无客制化时返回空列表
     */
    /**
     * 从 skuId 中提取商品ID。格式非法时抛出 {@link BizError}。
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
     * 从 skuId 中提取客制化项目ID列表（与 {@link #parseOptionIds} 一一对应）。
     * 格式非法时抛出 {@link BizError}。
     *
     * @param skuId SKU 标识
     * @return 客制化项目ID列表；无客制化时返回空列表
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

    private static long parseLong(String value, String skuId) {
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            throw new BizError(OrderErrorCode.SKU_INVALID, "skuId 格式非法: " + skuId);
        }
    }
}
