package cn.dextea.trade.catalog.domain.model.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Map;

@Getter
@EqualsAndHashCode
public final class ProductStoreStatusCode {

    public static final ProductStoreStatusCode UNAVAILABLE = new ProductStoreStatusCode(0, "门店不可售");
    public static final ProductStoreStatusCode AVAILABLE = new ProductStoreStatusCode(1, "门店可售");

    private static final Map<Integer, ProductStoreStatusCode> CACHE = Map.of(
            0, UNAVAILABLE,
            1, AVAILABLE
    );

    private final int code;
    private final String description;

    private ProductStoreStatusCode(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static ProductStoreStatusCode of(int code) {
        ProductStoreStatusCode status = CACHE.get(code);
        if (status == null) {
            throw new IllegalArgumentException("非法的商品门店状态枚举值: " + code);
        }
        return status;
    }

    @Override
    public String toString() {
        return Integer.toString(code);
    }
}
