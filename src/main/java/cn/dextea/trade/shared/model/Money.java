package cn.dextea.trade.shared.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Getter
@EqualsAndHashCode
public final class Money {

    public static final int SCALE = 2;
    public static final Money ZERO = new Money(BigDecimal.ZERO);

    private final BigDecimal value;

    private Money(BigDecimal value) {
        this.value = value;
    }

    public static Money of(BigDecimal value) {
        if (value == null) {
            throw new IllegalArgumentException("金额不能为空");
        }
        BigDecimal scaled = value.setScale(SCALE, RoundingMode.HALF_UP);
        if (scaled.signum() < 0) {
            throw new IllegalArgumentException("金额不能为负数: " + value);
        }
        return new Money(scaled);
    }

    public Money add(Money other) {
        return new Money(this.value.add(other.value));
    }

    public Money subtract(Money other) {
        return new Money(this.value.subtract(other.value));
    }

    public Money multiply(int factor) {
        return new Money(this.value.multiply(BigDecimal.valueOf(factor)));
    }

    public Money multiply(Quantity quantity) {
        return multiply(quantity.getValue());
    }

    public boolean isGreaterThan(Money other) {
        return this.value.compareTo(other.value) > 0;
    }

    public boolean isNegative() {
        return this.value.signum() < 0;
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
