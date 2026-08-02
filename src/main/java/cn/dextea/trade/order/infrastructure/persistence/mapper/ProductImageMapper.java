package cn.dextea.trade.order.infrastructure.persistence.mapper;

import cn.dextea.trade.order.infrastructure.persistence.po.ProductImagePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ProductImageMapper {

    @Select("SELECT * FROM product_images WHERE product_id = #{productId} AND type = 1 "
            + "LIMIT 1")
    ProductImagePO selectCoverByProductId(@Param("productId") Long productId);
}
