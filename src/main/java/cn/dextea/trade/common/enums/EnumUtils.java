package cn.dextea.trade.common.enums;
public final class EnumUtils {
    private EnumUtils() {
    }
    public static <E extends CodeEnum> E of(Class<E> type, Integer code) {
        if (code == null) {
            throw new IllegalArgumentException("非法的 " + type.getSimpleName() + " 枚举值: null");
        }
        for (E e : type.getEnumConstants()) {
            if (e.getCode() == code) {
                return e;
            }
        }
        throw new IllegalArgumentException("非法的 " + type.getSimpleName() + " 枚举值: " + code);
    }
    public static <E extends StringCodeEnum> E of(Class<E> type, String code) {
        if (code == null) {
            throw new IllegalArgumentException("非法的 " + type.getSimpleName() + " 枚举值: null");
        }
        for (E e : type.getEnumConstants()) {
            if (code.equals(e.getValue())) {
                return e;
            }
        }
        throw new IllegalArgumentException("非法的 " + type.getSimpleName() + " 枚举值: " + code);
    }
}
