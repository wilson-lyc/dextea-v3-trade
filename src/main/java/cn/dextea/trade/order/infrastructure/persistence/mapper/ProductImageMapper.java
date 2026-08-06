package cn.dextea.trade.order.infrastructure.persistence.mapper;

import cn.dextea.trade.order.infrastructure.persistence.po.ProductImagePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Collection;
import java.util.List;

@Mapper
public interface ProductImageMapper {

    @Select("SELECT * FROM product_images WHERE product_id = #{productId} AND type = 1 "
            + "LIMIT 1")
    ProductImagePO selectCoverByProductId(@Param("productId") Long productId);

    @Select("<script>SELECT * FROM product_images WHERE type = 1 AND product_id IN "
            + "<foreach collection='productIds' item='pid' open='(' separator=',' close=')'>#{pid}</foreach>"
            + "</script>")
    List<ProductImagePO> selectCoversByProductIds(@Param("productIds") Collection<Long> productIds);
}
