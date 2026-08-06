package cn.dextea.trade.shared.domain.error;

public final class Ensure {
    private Ensure() {
    }

    public static <T> T notNull(T object, BizErrorCode errorCode) {
        if (object == null) {
            throw new BizError(errorCode);
        }
        return object;
    }

    public static <T> T notNull(T object, BizErrorCode errorCode, String message) {
        if (object == null) {
            throw new BizError(errorCode, message);
        }
        return object;
    }

    public static <T extends Activatable> T active(T entity) {
        if (entity == null || !entity.isActive()) {
            throw new BizError(entity.inactiveErrorCode());
        }
        return entity;
    }
}
