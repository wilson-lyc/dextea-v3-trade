package cn.dextea.trade.common;

public abstract class BizErrorCode {

    private final int code;

    private final String message;

    protected BizErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
