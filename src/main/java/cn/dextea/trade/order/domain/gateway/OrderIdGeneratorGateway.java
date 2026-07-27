package cn.dextea.trade.order.domain.gateway;

/**
 * 订单号生成网关（由基础设施层适配 cosid 命名生成器 {@code order}）。
 */
public interface OrderIdGeneratorGateway {

    String generateOrderNo();
}
