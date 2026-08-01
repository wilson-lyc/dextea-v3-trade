package cn.dextea.trade.order.domain.model.valueobject;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public final class PickupCode {

    public static final PickupCode EMPTY = new PickupCode(null);

    private final String value;

    private PickupCode(String value) {
        this.value = value;
    }

    public static PickupCode of(String value) {
        if (value == null || value.isBlank()) {
            return EMPTY;
        }
        return new PickupCode(value);
    }

    public boolean isEmpty() {
        return value == null || value.isBlank();
    }

    @Override
    public String toString() {
        return value;
    }
}
