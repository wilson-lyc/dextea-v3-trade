package cn.dextea.trade.shared.error;
import lombok.Getter;
@Getter
public class BizError extends RuntimeException {
    private final BizErrorCode errorCode;
    private final int code;
    public BizError(BizErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.code = errorCode.getCode();
    }
    public BizError(BizErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.code = errorCode.getCode();
    }
    public BizError(BizErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
        this.code = errorCode.getCode();
    }
    public BizError(BizErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.code = errorCode.getCode();
    }
    public BizError(int code, String message) {
        super(message);
        this.errorCode = null;
        this.code = code;
    }
    public int getCode() {
        return code;
    }
}
