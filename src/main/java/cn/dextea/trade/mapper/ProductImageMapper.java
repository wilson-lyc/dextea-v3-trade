package cn.dextea.trade.mapper;

import cn.dextea.trade.entity.ProductImage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ProductImageMapper {

    /**
     * 批量查询商品的封面图（type=1）。封面图至多 1 张，若数据库存在多张则按 sort、created_at、image_id 升序取第一张。
     * 返回结果由调用方按 product_id 去重保留首条，得到 productId -> imageId(coverId) 的映射。
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
