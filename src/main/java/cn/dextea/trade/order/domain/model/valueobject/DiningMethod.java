package cn.dextea.trade.order.domain.model.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Map;

@Getter
@EqualsAndHashCode
public final class DiningMethod {

    public static final DiningMethod DINE_IN = new DiningMethod(1, "堂食");
    public static final DiningMethod TAKEOUT = new DiningMethod(2, "外带");

    private static final Map<Integer, DiningMethod> CACHE = Map.of(
            1, DINE_IN,
            2, TAKEOUT
    );

    private final int code;
    private final String description;

    private DiningMethod(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static DiningMethod of(int code) {
        DiningMethod method = CACHE.get(code);
        if (method == null) {
            throw new IllegalArgumentException("非法的用餐方式枚举值: " + code);
        }
        return method;
    }

    @Override
    public String toString() {
        return Integer.toString(code);
    }
}
