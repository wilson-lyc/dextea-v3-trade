package cn.dextea.trade.shared.util;

import cn.dextea.trade.shared.error.BizError;
import cn.dextea.trade.shared.error.BizErrorCode;
import cn.dextea.trade.shared.error.CommonErrorCode;

public final class EnsureUtil {
    private EnsureUtil() {
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
}
