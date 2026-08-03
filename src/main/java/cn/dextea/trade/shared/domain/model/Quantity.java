package cn.dextea.trade.shared.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public final class Quantity {

    private final int value;

    public static final Quantity ZERO = new Quantity(0);

    private Quantity(int value) {
        if (value < 0) {
            throw new IllegalArgumentException("数量不能为负数: " + value);
        }
        this.value = value;
    }

    public static Quantity of(int value) {
        if (value < 0) {
            throw new IllegalArgumentException("数量不能为负数: " + value);
        }
        return new Quantity(value);
    }

    public Quantity add(Quantity other) {
        return new Quantity(this.value + other.value);
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }
}
