package cn.dextea.trade.order.infrastructure.persistence.mapper;

import cn.dextea.trade.order.infrastructure.persistence.po.CustomizationOptionStoreStatusPO;
import cn.dextea.trade.order.infrastructure.persistence.po.ProductStoreStatusPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface StoreStatusMapper {

    // 懒加载表：无记录表示门店状态为 0-禁用，仅在发生写操作后才产生记录。
    // 调用方组装状态时应以缺失记录默认 status=0 处理。
    @Select("<script>" +
            "SELECT product_id AS productId, store_id AS storeId, status, created_at AS createdAt, updated_at AS updatedAt " +
            "FROM product_store_status " +
            "WHERE store_id = #{storeId} " +
            "AND product_id IN " +
            "<foreach collection='productIds' item='id' open='(' separator=',' close=')'>(#{id})</foreach>" +
            "</script>")
    List<ProductStoreStatusPO> selectProductStoreStatus(@Param("productIds") List<Long> productIds,
                                                        @Param("storeId") Long storeId);

    @Select("<script>" +
            "SELECT option_id AS optionId, store_id AS storeId, status, created_at AS createdAt, updated_at AS updatedAt " +
            "FROM customization_option_store_status " +
            "WHERE store_id = #{storeId} " +
            "AND option_id IN " +
            "<foreach collection='optionIds' item='id' open='(' separator=',' close=')'>(#{id})</foreach>" +
            "</script>")
    List<CustomizationOptionStoreStatusPO> selectCustomizationOptionStoreStatus(
            @Param("optionIds") List<Long> optionIds, @Param("storeId") Long storeId);
}
