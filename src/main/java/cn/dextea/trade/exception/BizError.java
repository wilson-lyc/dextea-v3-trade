package cn.dextea.trade.exception;

import cn.dextea.trade.common.BizErrorCode;
import lombok.Getter;

/**
 * 自定义业务异常：携带业务错误码 {@link BizErrorCode}，由全局异常拦截器统一转换为响应返回。
 */
@Getter
public class BizError extends RuntimeException {

    private final BizErrorCode errorCode;

    public BizError(BizErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public BizError(BizErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public int getCode() {
        return errorCode.getCode();
    }
}
