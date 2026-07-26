package cn.dextea.trade.order.domain.port;

import java.util.function.Supplier;

/**
 * 订单分布式锁端口（由基础设施层适配 Redis 锁）。
 */
public interface OrderLockPort {

    /**
     * 在订单锁保护下执行业务逻辑。
     *
     * @param orderNo  订单号（作为锁粒度 key 的一部分）
     * @param supplier 业务逻辑
     * @param <T>      返回类型
     * @return 业务逻辑返回值
     */
    <T> T executeWithLock(String orderNo, Supplier<T> supplier);
}
