package cn.dextea.trade.order.domain.port;

public interface IdempotencyStore {

    boolean exists(String idempotencyKey);

    void record(String idempotencyKey, String orderNo);
}
