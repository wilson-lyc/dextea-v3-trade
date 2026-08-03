package cn.dextea.trade.shared.domain.error;

/**
 * 跨模块通用错误码。100xxx 段保留给 shared/通用错误。
 */
public enum CommonErrorCode implements BizErrorCode {
    MISSING_REQUEST_HEADER(100001, "缺少请求头"),
    MYBATIS_SYSTEM_EXCEPTION(100002, "系统繁忙，请稍后重试");

    private final int code;
    private final String message;

    CommonErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
