package cn.dextea.trade.order.domain.gateway;
import java.util.function.Supplier;
public interface OrderLockGateway {
    <T> T executeWithLock(String orderNo, Supplier<T> supplier);
}
