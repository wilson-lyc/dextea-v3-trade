package cn.dextea.trade.order.domain.port;

import java.time.Duration;

public interface OrderCreateLock {

    boolean tryLock(String lockKey, String token, Duration duration);

    void unlock(String lockKey, String token);
}
