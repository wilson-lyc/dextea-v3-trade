package cn.dextea.trade.catalog.domain.model.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Map;

@Getter
@EqualsAndHashCode
public final class StoreStatus {

    public static final StoreStatus CLOSED = new StoreStatus(0, "停业");
    public static final StoreStatus OPEN = new StoreStatus(1, "营业");

    private static final Map<Integer, StoreStatus> CACHE = Map.of(
            0, CLOSED,
            1, OPEN
    );

    private final int code;
    private final String description;

    private StoreStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static StoreStatus of(int code) {
        StoreStatus status = CACHE.get(code);
        if (status == null) {
            throw new IllegalArgumentException("非法的门店状态枚举值: " + code);
        }
        return status;
    }

    @Override
    public String toString() {
        return Integer.toString(code);
    }
}
