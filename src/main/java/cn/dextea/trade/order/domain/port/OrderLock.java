package cn.dextea.trade.order.domain.port;

import java.util.function.Supplier;

public interface OrderLock {

    <T> T executeWithLock(String orderNo, Supplier<T> supplier);
}
