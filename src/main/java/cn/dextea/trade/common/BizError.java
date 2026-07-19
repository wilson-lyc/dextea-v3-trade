package cn.dextea.trade.common;

import lombok.Getter;

/**
 * 业务异常。
 *
 * <p>当业务校验不通过或业务规则无法满足时抛出，由 {@link GlobalExceptionHandler}
 * 统一转换为 {@link APIResponse} 反馈给前端。</p>
 *
 * <p>本类只负责“被抛出、携带栈信息”，具体的业务码与默认文案应当集中维护在
 * 实现 {@link BizErrorCode} 的枚举中（如 {@code OrderErrorCode}），通过构造方法传入，
 * 避免在业务代码里散落硬编码的错误码与文案。</p>
 */
@Getter
public class BizError extends RuntimeException {

    /** 关联的错误码定义，是业务码与默认文案的唯一来源。 */
    private final BizErrorCode errorCode;

    /**
     * 使用错误码中的默认文案作为异常信息。
     */
    public BizError(BizErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    /**
     * 使用自定义文案覆盖默认文案（例如需要携带商品名、ID 等动态信息）。
     */
    public BizError(BizErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /** 业务码，委托给 {@link BizErrorCode#getCode()}。 */
    public int getCode() {
        return errorCode.getCode();
    }
}
