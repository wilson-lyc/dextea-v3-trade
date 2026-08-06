package cn.dextea.trade.shared.error;

public final class Ensure {
    private Ensure() {
    }

    public static <T> T notNull(T object) {
        if (object == null) {
            throw new BizError(CommonErrorCode.NOT_FOUND);
        }
        return object;
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

    public static <T> T notNull(T object, int code, String message) {
        if (object == null) {
            throw new BizError(code, message);
        }
        return object;
    }

    public static <T extends Activatable> T active(T entity) {
        if (entity == null || !entity.isActive()) {
            throw new BizError(CommonErrorCode.NOT_FOUND);
        }
        return entity;
    }

    public static <T extends Activatable> T active(T entity, BizErrorCode errorCode) {
        if (entity == null || !entity.isActive()) {
            throw new BizError(errorCode);
        }
        return entity;
    }

    public static <T extends Activatable> T active(T entity, BizErrorCode errorCode, String message) {
        if (entity == null || !entity.isActive()) {
            throw new BizError(errorCode, message);
        }
        return entity;
    }

    public static <T extends Activatable> T active(T entity, int code, String message) {
        if (entity == null || !entity.isActive()) {
            throw new BizError(code, message);
        }
        return entity;
    }
}
