package cn.dextea.trade.catalog.domain.model.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Map;

@Getter
@EqualsAndHashCode
public final class CustomizationOptionStoreStatusCode {

    public static final CustomizationOptionStoreStatusCode UNAVAILABLE = new CustomizationOptionStoreStatusCode(0, "门店不可用");
    public static final CustomizationOptionStoreStatusCode AVAILABLE = new CustomizationOptionStoreStatusCode(1, "门店可用");

    private static final Map<Integer, CustomizationOptionStoreStatusCode> CACHE = Map.of(
            0, UNAVAILABLE,
            1, AVAILABLE
    );

    private final int code;
    private final String description;

    private CustomizationOptionStoreStatusCode(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static CustomizationOptionStoreStatusCode of(int code) {
        CustomizationOptionStoreStatusCode status = CACHE.get(code);
        if (status == null) {
            throw new IllegalArgumentException("非法的客制化选项门店状态枚举值: " + code);
        }
        return status;
    }

    @Override
    public String toString() {
        return Integer.toString(code);
    }
}
