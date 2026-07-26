package cn.dextea.trade.common.error;

import lombok.Getter;

public class DomainException extends BizError {

    public DomainException(BizErrorCode errorCode) {
        super(errorCode);
    }

    public DomainException(BizErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public DomainException(BizErrorCode errorCode, Throwable cause) {
        super(errorCode, errorCode.getMessage(), cause);
    }

    public DomainException(BizErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}
