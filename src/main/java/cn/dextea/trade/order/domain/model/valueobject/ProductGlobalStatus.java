package cn.dextea.trade.order.domain.model.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Map;

@Getter
@EqualsAndHashCode
public final class ProductGlobalStatus {

    public static final ProductGlobalStatus OFF_SHELF = new ProductGlobalStatus(0, "下架");
    public static final ProductGlobalStatus ON_SHELF = new ProductGlobalStatus(1, "上架");

    private static final Map<Integer, ProductGlobalStatus> CACHE = Map.of(
            0, OFF_SHELF,
            1, ON_SHELF
    );

    private final int code;
    private final String description;

    private ProductGlobalStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static ProductGlobalStatus of(int code) {
        ProductGlobalStatus status = CACHE.get(code);
        if (status == null) {
            throw new IllegalArgumentException("非法的商品全局状态枚举值: " + code);
        }
        return status;
    }

    @Override
    public String toString() {
        return Integer.toString(code);
    }
}
