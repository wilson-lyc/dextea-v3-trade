package cn.dextea.trade.order.domain.exception;
import cn.dextea.trade.shared.domain.error.BizErrorCode;
public enum OrderErrorCode implements BizErrorCode {
    CUSTOMER_NOT_FOUND(101001, "顾客不存在"),
    STORE_NOT_FOUND(101002, "门店不存在"),
    PRODUCT_NOT_FOUND(101003, "商品不存在"),
    CUSTOMIZATION_ITEM_NOT_FOUND(101004, "客制化项目不存在"),
    CUSTOMIZATION_OPTION_NOT_FOUND(101005, "客制化选项不存在");
    
    private final int code;
    private final String message;

    OrderErrorCode(int code, String message) {
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
