package cn.dextea.trade.order.domain.gateway;

/**
 * 商品封面网关返回值：由基础设施层完成 product_image → gallery 的清洗后提供。
 *
 * <p>领域层只关心「哪个商品、封面标识是什么、封面 URL 是什么」，
 * 不关心底层图库表结构。coverId 为持久化用的不透明封面标识。</p>
 *
 * @param productId 商品 ID
 * @param coverId   封面标识（随订单明细持久化）
 * @param coverUrl  封面展示 URL
 */
public record ProductCover(Long productId, Long coverId, String coverUrl) {
}
