package cn.dextea.trade.shared.error;

/**
 * 跨模块通用错误码，按首位区分错误大类：
 * 2xxxxx 业务错误 / 3xxxxx 第三方下游错误 / 4xxxxx 客户端错误 / 5xxxxx 系统错误。
 * 4 段内小分类与 HTTP 4xx 语义对齐：400xx 参数 / 401xx 鉴权 / 404xx 资源不存在 / 409xx 冲突幂等 / 429xx 限流。
 * 完整规范见 docs/api/README.md。
 */
public enum CommonErrorCode implements BizErrorCode {
    // 4xxxx 客户端错误
    MISSING_REQUEST_HEADER(40001, "缺少请求头"),
    PARAM_MISSING(40002, "参数缺失"),
    NOT_FOUND(40400, "资源不存在"),
    UNAUTHORIZED(40100, "未登录"),
    DUPLICATE_SUBMIT(40901, "重复提交，请勿重复操作"),
    TOO_FREQUENT(42901, "请求过于频繁"),

    // 5xxxx 系统错误
    SYSTEM_ERROR(50000, "系统繁忙，请稍后重试"),
    DB_NOT_ENABLED(50100, "数据库未启用"),
    MYBATIS_SYSTEM_EXCEPTION(50101, "系统繁忙，请稍后重试");

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
