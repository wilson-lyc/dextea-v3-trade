package cn.dextea.trade.shared.error;

public enum DownstreamErrorCode implements BizErrorCode {
    DOWNSTREAM_NOT_CONFIGURED(30001, "下游服务未配置"),
    DOWNSTREAM_UNAVAILABLE(30002, "下游服务不可用");

    private final int code;
    private final String message;

    DownstreamErrorCode(int code, String message) {
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
