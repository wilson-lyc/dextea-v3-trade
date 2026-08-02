package cn.dextea.trade.order.infrastructure.persistence.mapper;

import cn.dextea.trade.order.infrastructure.persistence.po.ProductStoreStatusPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ProductStoreStatusMapper {

    @Select("SELECT * FROM product_store_status WHERE product_id = #{productId} AND store_id = #{storeId}")
    ProductStoreStatusPO selectByProductIdAndStoreId(@Param("productId") Long productId,
                                                     @Param("storeId") Long storeId);
}
