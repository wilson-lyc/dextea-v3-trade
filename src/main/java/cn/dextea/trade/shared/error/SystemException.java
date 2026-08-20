package cn.dextea.trade.shared.error;

import lombok.Getter;

@Getter
public class SystemException extends RuntimeException {

    private final BizErrorCode errorCode;
    private final int code;

    public SystemException(BizErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.code = errorCode.getCode();
    }

    public SystemException(BizErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.code = errorCode.getCode();
    }

    public SystemException(BizErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
        this.code = errorCode.getCode();
    }

    public SystemException(BizErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.code = errorCode.getCode();
    }

    public SystemException(int code, String message) {
        super(message);
        this.errorCode = null;
        this.code = code;
    }
}
