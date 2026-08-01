package cn.dextea.trade.order.infrastructure.persistence.mapper;

import cn.dextea.trade.order.infrastructure.persistence.po.ProductCoverPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ProductImageMapper {

    @Select("<script>" +
            "SELECT productId, imageId, url FROM (" +
            "SELECT pi.product_id AS productId, pi.image_id AS imageId, g.url AS url, " +
            "ROW_NUMBER() OVER (PARTITION BY pi.product_id ORDER BY pi.sort ASC, pi.created_at ASC, pi.image_id ASC) AS rn " +
            "FROM product_images pi " +
            "LEFT JOIN gallery g ON g.id = pi.image_id " +
            "WHERE pi.type = 1 " +
            "AND pi.product_id IN " +
            "<foreach collection='productIds' item='id' open='(' separator=',' close=')'>(#{id})</foreach>" +
            ") t WHERE rn = 1" +
            "</script>")
    List<ProductCoverPO> selectCoversByProductIds(@Param("productIds") List<Long> productIds);
}
