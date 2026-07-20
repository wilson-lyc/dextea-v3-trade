package cn.dextea.trade.common;

import lombok.Getter;

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
