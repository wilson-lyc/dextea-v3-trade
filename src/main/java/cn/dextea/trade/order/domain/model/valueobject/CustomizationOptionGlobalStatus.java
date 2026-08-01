package cn.dextea.trade.order.domain.model.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Map;

@Getter
@EqualsAndHashCode
public final class CustomizationOptionGlobalStatus {

    public static final CustomizationOptionGlobalStatus DISABLED = new CustomizationOptionGlobalStatus(0, "禁用");
    public static final CustomizationOptionGlobalStatus ACTIVE = new CustomizationOptionGlobalStatus(1, "激活");

    private static final Map<Integer, CustomizationOptionGlobalStatus> CACHE = Map.of(
            0, DISABLED,
            1, ACTIVE
    );

    private final int code;
    private final String description;

    private CustomizationOptionGlobalStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static CustomizationOptionGlobalStatus of(int code) {
        CustomizationOptionGlobalStatus status = CACHE.get(code);
        if (status == null) {
            throw new IllegalArgumentException("非法的客制化选项全局状态枚举值: " + code);
        }
        return status;
    }

    @Override
    public String toString() {
        return Integer.toString(code);
    }
}
