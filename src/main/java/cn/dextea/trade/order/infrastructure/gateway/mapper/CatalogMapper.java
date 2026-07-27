package cn.dextea.trade.order.infrastructure.gateway.mapper;

import cn.dextea.trade.order.infrastructure.gateway.po.CustomerPO;
import cn.dextea.trade.order.infrastructure.gateway.po.CustomizationOptionPO;
import cn.dextea.trade.order.infrastructure.gateway.po.CustomizationOptionStoreStatusPO;
import cn.dextea.trade.order.infrastructure.gateway.po.CustomizationPO;
import cn.dextea.trade.order.infrastructure.gateway.po.GalleryPO;
import cn.dextea.trade.order.infrastructure.gateway.po.ProductImagePO;
import cn.dextea.trade.order.infrastructure.gateway.po.ProductPO;
import cn.dextea.trade.order.infrastructure.gateway.po.ProductStoreStatusPO;
import cn.dextea.trade.order.infrastructure.gateway.po.StorePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 外部支撑域（商品/客制化/门店/顾客）只读 Mapper：ACL 落地的数据来源。
 *
 * <p>仅返回基础设施层 PO，由各 Translator 清洗为领域值对象后经网关提供给订单领域。</p>
 */
@Mapper
public interface CatalogMapper {

    @Select("<script>" +
            "SELECT id, name, status, price " +
            "FROM products " +
            "WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>(#{id})</foreach>" +
            "</script>")
    List<ProductPO> selectProductsByIds(@Param("ids") List<Long> ids);

    @Select("<script>" +
            "SELECT product_id AS productId, image_id AS imageId, type, sort, created_at AS createdAt " +
            "FROM product_images " +
            "WHERE type = 1 " +
            "AND product_id IN " +
            "<foreach collection='productIds' item='id' open='(' separator=',' close=')'>(#{id})</foreach>" +
            "ORDER BY product_id, sort ASC, created_at ASC, image_id ASC" +
            "</script>")
    List<ProductImagePO> selectCoverImagesByProductIds(@Param("productIds") List<Long> productIds);

    @Select("<script>" +
            "SELECT id, url, object_key AS objectKey, created_at AS createdAt, name " +
            "FROM gallery " +
            "WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>(#{id})</foreach>" +
            "</script>")
    List<GalleryPO> selectGalleriesByIds(@Param("ids") List<Long> ids);

    @Select("<script>" +
            "SELECT product_id AS productId, store_id AS storeId, status " +
            "FROM product_store_status " +
            "WHERE store_id = #{storeId} " +
            "AND product_id IN " +
            "<foreach collection='productIds' item='id' open='(' separator=',' close=')'>(#{id})</foreach>" +
            "</script>")
    List<ProductStoreStatusPO> selectProductStoreStatusByProductIdsAndStoreId(@Param("productIds") List<Long> productIds,
                                                                              @Param("storeId") Long storeId);

    @Select("<script>" +
            "SELECT id, product_id AS productId, name, status " +
            "FROM customizations " +
            "WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>(#{id})</foreach>" +
            "</script>")
    List<CustomizationPO> selectCustomizationsByIds(@Param("ids") List<Long> ids);

    @Select("<script>" +
            "SELECT id, customization_id AS customizationId, name, price, status " +
            "FROM customization_options " +
            "WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>(#{id})</foreach>" +
            "</script>")
    List<CustomizationOptionPO> selectCustomizationOptionsByIds(@Param("ids") List<Long> ids);

    @Select("<script>" +
            "SELECT customization_option_id AS customizationOptionId, store_id AS storeId, status " +
            "FROM customization_option_store_status " +
            "WHERE store_id = #{storeId} " +
            "AND customization_option_id IN " +
            "<foreach collection='optionIds' item='id' open='(' separator=',' close=')'>(#{id})</foreach>" +
            "</script>")
    List<CustomizationOptionStoreStatusPO> selectOptionStoreStatusByOptionIdsAndStoreId(
            @Param("optionIds") List<Long> optionIds, @Param("storeId") Long storeId);

    @Select("SELECT id, name, status, address, phone, business_hours AS businessHours " +
            "FROM stores WHERE id = #{id}")
    StorePO selectStoreById(@Param("id") Long id);

    @Select("SELECT id, name, status, alipay_open_id AS alipayOpenId, weixin_open_id AS weixinOpenId " +
            "FROM customers WHERE id = #{id}")
    CustomerPO selectCustomerById(@Param("id") Long id);
}
