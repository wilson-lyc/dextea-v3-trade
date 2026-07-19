package cn.dextea.trade.error;

import cn.dextea.trade.common.BizErrorCode;

public final class OrderBizErrorCode {

    private OrderBizErrorCode() {
    }

    public static final BizErrorCode NOT_FOUND = new BizErrorCode(10001, "订单不存在") {};
    public static final BizErrorCode CREATE_FAILED = new BizErrorCode(10002, "订单创建失败") {};
    public static final BizErrorCode CALCULATE_FAILED = new BizErrorCode(10003, "订单金额计算失败") {};
    public static final BizErrorCode ITEM_EMPTY = new BizErrorCode(10004, "订单明细为空") {};
    public static final BizErrorCode PRODUCT_OFF_SHELF = new BizErrorCode(10005, "商品已下架") {};
    public static final BizErrorCode PRODUCT_SOLD_OUT = new BizErrorCode(10006, "商品售罄") {};
}
