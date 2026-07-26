package cn.dextea.trade.order.domain.port;

/**
 * 订单号生成端口（由基础设施层适配 cosid 命名生成器 {@code order}）。
 */
public interface OrderIdGeneratorPort {

    String generateOrderNo();
}
