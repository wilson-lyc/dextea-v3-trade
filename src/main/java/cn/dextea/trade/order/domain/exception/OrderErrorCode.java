package cn.dextea.trade.order.domain.exception;
import cn.dextea.trade.shared.domain.error.BizErrorCode;
public enum OrderErrorCode implements BizErrorCode {
    CUSTOMER_NOT_FOUND(101001, "顾客不存在"),
    STORE_NOT_FOUND(101002, "门店不存在"),
    PRODUCT_NOT_FOUND(101003, "商品不存在"),
    CUSTOMIZATION_ITEM_NOT_FOUND(101004, "客制化项目不存在"),
    CUSTOMIZATION_OPTION_NOT_FOUND(101005, "客制化选项不存在"),
    STORE_INACTIVE(101006, "门店未营业"),
    CUSTOMER_INACTIVE(101007, "顾客不可用"),
    INVALID_SKU(101008, "非法的SKU"),
    INVALID_BINDING(101009, "非法的绑定关系"),
    INVALID_DINING_METHOD(101010, "非法的用餐方式"),
    INVALID_ORDER_SOURCE(101011, "非法的订单来源"),
    INVALID_PAYMENT_METHOD(101012, "非法的支付方式"),
    INVALID_ORDER_ITEM_QUANTITY(101013, "订单项数量非法");
    
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
