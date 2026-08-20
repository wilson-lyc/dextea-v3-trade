package cn.dextea.trade.shared.error;

/**
 * 跨模块通用错误码，按首段区分错误大类：
 * 1xxxxx 系统错误 / 4xxxxx 参数校验错误 / 5xxxxx 限流幂等熔断。
 */
public enum CommonErrorCode implements BizErrorCode {
    SYSTEM_ERROR(10000, "系统繁忙，请稍后重试"),
    DB_NOT_ENABLED(10300, "数据库未启用"),
    MYBATIS_SYSTEM_EXCEPTION(10301, "系统繁忙，请稍后重试"),

    NOT_FOUND(40001, "资源不存在"),
    MISSING_REQUEST_HEADER(40001, "缺少请求头"),
    PARAM_MISSING(40002, "参数缺失"),
    UNAUTHORIZED(40100, "未登录"),

    TOO_FREQUENT(50001, "请求过于频繁"),
    DUPLICATE_SUBMIT(50002, "重复提交，请勿重复操作");

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
