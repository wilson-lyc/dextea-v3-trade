package cn.dextea.trade.catalog.infrastructure.persistence;

import cn.dextea.trade.catalog.domain.model.Customization;
import cn.dextea.trade.catalog.domain.model.CustomizationOption;
import cn.dextea.trade.catalog.domain.model.CustomizationOptionStoreStatus;
import cn.dextea.trade.catalog.domain.model.Customer;
import cn.dextea.trade.catalog.domain.model.Gallery;
import cn.dextea.trade.catalog.domain.model.Product;
import cn.dextea.trade.catalog.domain.model.ProductImage;
import cn.dextea.trade.catalog.domain.model.ProductStoreStatus;
import cn.dextea.trade.catalog.domain.model.Store;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 商品目录读模型 Mapper：聚合原 9 个零散 Mapper 的批量查询，供 {@link CatalogPersistenceAdapter} 使用。
 */
@Mapper
public interface CatalogMapper {

    @Select("<script>" +
            "SELECT id, name, status, price " +
            "FROM products " +
            "WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>(#{id})</foreach>" +
            "</script>")
    List<Product> selectProductsByIds(@Param("ids") List<Long> ids);

    @Select("<script>" +
            "SELECT product_id AS productId, image_id AS imageId, type, sort, created_at AS createdAt " +
            "FROM product_images " +
            "WHERE type = 1 " +
            "AND product_id IN " +
            "<foreach collection='productIds' item='id' open='(' separator=',' close=')'>(#{id})</foreach>" +
            "ORDER BY product_id, sort ASC, created_at ASC, image_id ASC" +
            "</script>")
    List<ProductImage> selectCoverImagesByProductIds(@Param("productIds") List<Long> productIds);

    @Select("<script>" +
            "SELECT id, url, object_key AS objectKey, created_at AS createdAt, name " +
            "FROM gallery " +
            "WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>(#{id})</foreach>" +
            "</script>")
    List<Gallery> selectGalleriesByIds(@Param("ids") List<Long> ids);

    @Select("<script>" +
            "SELECT product_id AS productId, store_id AS storeId, status " +
            "FROM product_store_status " +
            "WHERE store_id = #{storeId} " +
            "AND product_id IN " +
            "<foreach collection='productIds' item='id' open='(' separator=',' close=')'>(#{id})</foreach>" +
            "</script>")
    List<ProductStoreStatus> selectProductStoreStatusByProductIdsAndStoreId(@Param("productIds") List<Long> productIds,
                                                                            @Param("storeId") Long storeId);

    @Select("<script>" +
            "SELECT id, product_id AS productId, name, status " +
            "FROM customizations " +
            "WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>(#{id})</foreach>" +
            "</script>")
    List<Customization> selectCustomizationsByIds(@Param("ids") List<Long> ids);

    @Select("<script>" +
            "SELECT id, customization_id AS customizationId, name, price, status " +
            "FROM customization_options " +
            "WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>(#{id})</foreach>" +
            "</script>")
    List<CustomizationOption> selectCustomizationOptionsByIds(@Param("ids") List<Long> ids);

    @Select("<script>" +
            "SELECT customization_option_id AS customizationOptionId, store_id AS storeId, status " +
            "FROM customization_option_store_status " +
            "WHERE store_id = #{storeId} " +
            "AND customization_option_id IN " +
            "<foreach collection='optionIds' item='id' open='(' separator=',' close=')'>(#{id})</foreach>" +
            "</script>")
    List<CustomizationOptionStoreStatus> selectOptionStoreStatusByOptionIdsAndStoreId(
            @Param("optionIds") List<Long> optionIds, @Param("storeId") Long storeId);

    @Select("SELECT id, name, status FROM stores WHERE id = #{id}")
    Store selectStoreById(@Param("id") Long id);

    @Select("SELECT id, name, status, alipay_open_id, weixin_open_id FROM customers WHERE id = #{id}")
    Customer selectCustomerById(@Param("id") Long id);
}
