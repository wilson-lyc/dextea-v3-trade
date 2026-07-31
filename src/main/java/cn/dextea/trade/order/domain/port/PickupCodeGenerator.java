package cn.dextea.trade.order.domain.port;

public interface PickupCodeGenerator {
    String next(Long storeId);
}
