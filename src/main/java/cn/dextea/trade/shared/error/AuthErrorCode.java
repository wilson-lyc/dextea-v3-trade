package cn.dextea.trade.shared.error;

public enum AuthErrorCode implements BizErrorCode {
    TOKEN_INVALID(40101, "令牌无效"),
    TOKEN_EXPIRED(40102, "令牌已过期"),
    TOKEN_DISABLED(40103, "令牌已被禁用"),
    CONSOLE_UNAUTHORIZED(40104, "控制台未登录或会话已失效"),
    CONSOLE_CREDENTIAL_ERROR(40105, "控制台账号或密码错误");

    private final int code;
    private final String message;

    AuthErrorCode(int code, String message) {
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
