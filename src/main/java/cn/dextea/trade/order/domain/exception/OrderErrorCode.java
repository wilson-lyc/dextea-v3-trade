package cn.dextea.trade.order.domain.exception;
import cn.dextea.trade.shared.error.BizErrorCode;
public enum OrderErrorCode implements BizErrorCode {
    CUSTOMER_NOT_FOUND(20001, "顾客不存在"),
    STORE_NOT_FOUND(20002, "门店不存在"),
    PRODUCT_NOT_FOUND(20003, "商品不存在"),
    CUSTOMIZATION_ITEM_NOT_FOUND(20004, "客制化项目不存在"),
    CUSTOMIZATION_OPTION_NOT_FOUND(20005, "客制化选项不存在"),
    STORE_INACTIVE(20006, "门店未营业"),
    CUSTOMER_INACTIVE(20007, "顾客不可用"),
    INVALID_SKU(20008, "非法的SKU"),
    INVALID_BINDING(20009, "非法的绑定关系"),
    INVALID_DINING_METHOD(20010, "非法的用餐方式"),
    INVALID_ORDER_SOURCE(20011, "非法的订单来源"),
    INVALID_PAYMENT_METHOD(20012, "非法的支付方式"),
    INVALID_ORDER_ITEM_QUANTITY(20013, "订单项数量非法"),
    IDEMPOTENCY_KEY_CONFLICT(20014, "重复提交，请勿重复创建订单"),
    ORDER_NOT_FOUND(20016, "订单不存在"),
    ORDER_NOT_BELONG_TO_CUSTOMER(20017, "该订单不属于当前顾客"),
    ORDER_NOT_BELONG_TO_STORE(20019, "该订单不属于当前门店"),
    ORDER_UPDATE_CONFLICT(20018, "订单状态更新冲突，请重试"),
    ORDER_PAID_AMOUNT_MISMATCH(20020, "支付回调金额与订单金额不一致"),
    ORDER_NOT_PAID(20021, "仅已支付的订单可以开始制作"),
    ORDER_NOT_PREPARING(20022, "仅制作中的订单可以标记为制作完成"),
    ORDER_NOT_READY(20023, "仅制作完成的订单可以标记为已取餐"),
    ORDER_CANNOT_TIMEOUT(20024, "仅支付中的订单可以标记为支付超时"),
    ORDER_CANNOT_PAID(20025, "仅支付中的订单可以标记为已支付"),
    ORDER_CANNOT_REFUND(20026, "仅已支付的订单可以发起退款"),
    ORDER_INVALID_MAKING_TRANSITION(20027, "制作状态必须按 待制作->制作中->制作完成->已取餐 逐级变更"),
    ORDER_PAYMENT_PICKUP_CODE_REQUIRED(20028, "订单支付时必须包含取餐码");

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
