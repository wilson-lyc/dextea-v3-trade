package cn.dextea.trade.mapper;

import cn.dextea.trade.entity.ProductStoreStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ProductStoreStatusMapper {

    /**
     * 批量查询商品门店状态
     *
     * @param productIds 商品ID列表
     * @param storeId 门店ID
     * @return 商品门店状态列表
     */
    @Select("<script>" +
            "SELECT product_id AS productId, store_id AS storeId, status " +
            "FROM product_store_status " +
            "WHERE store_id = #{storeId} " +
            "AND product_id IN " +
            "<foreach collection='productIds' item='id' open='(' separator=',' close=')'>(#{id})</foreach>" +
            "</script>")
    List<ProductStoreStatus> selectByProductIdsAndStoreId(@Param("productIds") List<Long> productIds,
                                                          @Param("storeId") Long storeId);
}
