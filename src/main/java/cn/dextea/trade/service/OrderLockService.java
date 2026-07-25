package cn.dextea.trade.service;

import java.util.function.Supplier;

public interface OrderLockService {

    /**
     * 在订单锁保护下执行业务逻辑。
     *
     * @param orderNo  订单号（作为锁粒度 key 的一部分）
     * @param supplier 业务逻辑
     * @param <T>      返回类型
     * @return 业务逻辑返回值
     * @throws cn.dextea.trade.exception.BizError 获取锁失败时抛出
     *         {@link cn.dextea.trade.errorcode.OrderErrorCode#ORDER_LOCK_BUSY}
     */
    <T> T executeWithLock(String orderNo, Supplier<T> supplier);
}
