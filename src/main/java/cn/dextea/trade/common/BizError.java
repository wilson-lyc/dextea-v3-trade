package cn.dextea.trade.common;

import lombok.Getter;

@Getter
public class BizError extends RuntimeException {

    private final int code;

    public BizError(BizErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
    }

    public BizError(BizErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
    }

    public BizError(int code, String message) {
        super(message);
        this.code = code;
    }
}
