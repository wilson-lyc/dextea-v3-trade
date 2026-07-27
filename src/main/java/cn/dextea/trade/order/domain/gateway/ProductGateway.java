package cn.dextea.trade.order.domain.gateway;

import cn.dextea.trade.order.domain.model.valueobject.Product;
import cn.dextea.trade.order.domain.model.valueobject.ProductStoreStatus;

import java.util.List;
import java.util.Map;

/**
 * 商品网关：以只读快照形式向订单领域提供商品及其门店可用性、封面等数据。
 *
 * <p>图库（product_image / gallery）的表结构与两段 join 细节完全封装在基础设施层，
 * 领域层只消费「商品 → 封面标识 / 封面 URL」的清洗结果，不感知图库模型。</p>
 */
public interface ProductGateway {

    /** 批量获取商品只读快照。 */
    List<Product> findProducts(List<Long> ids);

    /** 批量获取商品在指定门店的可售状态。 */
    List<ProductStoreStatus> findProductStoreStatus(List<Long> productIds, Long storeId);

    /**
     * 批量获取商品封面（productId → 封面标识 + 封面 URL）。
     * <p>下单计价时使用：封面标识随订单明细持久化，封面 URL 用于预构建展示。</p>
     */
    Map<Long, ProductCover> findProductCovers(List<Long> productIds);

    /**
     * 批量将封面标识还原为封面 URL（coverId → url）。
     * <p>订单查询场景使用：明细已持久化 coverId，据此还原展示 URL。</p>
     */
    Map<Long, String> findCoverUrls(List<Long> coverIds);
}
