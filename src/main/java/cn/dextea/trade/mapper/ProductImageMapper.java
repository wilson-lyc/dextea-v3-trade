package cn.dextea.trade.mapper;

import cn.dextea.trade.entity.ProductImage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ProductImageMapper {

    /**
     * 批量查询商品封面图
     *
     * @param productIds 商品ID列表
     * @return 商品封面图列表
     */
    @Select("<script>" +
            "SELECT product_id AS productId, image_id AS imageId, type, sort, created_at AS createdAt " +
            "FROM product_images " +
            "WHERE type = 1 " +
            "AND product_id IN " +
            "<foreach collection='productIds' item='id' open='(' separator=',' close=')'>(#{id})</foreach>" +
            "ORDER BY product_id, sort ASC, created_at ASC, image_id ASC" +
            "</script>")
    List<ProductImage> selectCoverImagesByProductIds(@Param("productIds") List<Long> productIds);
}
