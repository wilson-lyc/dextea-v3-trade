package cn.dextea.trade.order.domain.model.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Map;

@Getter
@EqualsAndHashCode
public final class CustomizationStatus {

    public static final CustomizationStatus DISABLED = new CustomizationStatus(0, "禁用");
    public static final CustomizationStatus ACTIVE = new CustomizationStatus(1, "激活");

    private static final Map<Integer, CustomizationStatus> CACHE = Map.of(
            0, DISABLED,
            1, ACTIVE
    );

    private final int code;
    private final String description;

    private CustomizationStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static CustomizationStatus of(int code) {
        CustomizationStatus status = CACHE.get(code);
        if (status == null) {
            throw new IllegalArgumentException("非法的客制化状态枚举值: " + code);
        }
        return status;
    }

    @Override
    public String toString() {
        return Integer.toString(code);
    }
}
