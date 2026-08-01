package cn.dextea.trade.order.domain.model.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Map;

@Getter
@EqualsAndHashCode
public final class CustomerStatus {

    public static final CustomerStatus INACTIVE = new CustomerStatus(0, "未激活");
    public static final CustomerStatus ACTIVE = new CustomerStatus(1, "激活");

    private static final Map<Integer, CustomerStatus> CACHE = Map.of(
            0, INACTIVE,
            1, ACTIVE
    );

    private final int code;
    private final String description;

    private CustomerStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public static CustomerStatus of(int code) {
        CustomerStatus status = CACHE.get(code);
        if (status == null) {
            throw new IllegalArgumentException("非法的顾客状态枚举值: " + code);
        }
        return status;
    }

    @Override
    public String toString() {
        return Integer.toString(code);
    }
}
