package cn.dextea.trade.order.domain.gateway;

/**
 * 取餐码生成网关（由基础设施层适配 Redis 按门店维度的当日自增计数）。
 *
 * <p>生成规则：门店当日订单计数值对 100 取余，前缀拼接数字 "8"，
 * 例如计数值 11 → 取餐码 "8011"。</p>
 */
public interface PickupCodeGeneratorGateway {

    /**
     * 按门店维度生成当日取餐码（如 "8011"）；同一订单仅应调用一次。
     *
     * @param storeId 门店 ID
     * @return 取餐码，固定 4 位数字字符串
     */
    String generate(Long storeId);
}
