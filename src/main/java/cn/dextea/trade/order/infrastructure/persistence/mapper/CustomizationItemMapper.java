package cn.dextea.trade.order.infrastructure.persistence.mapper;

import cn.dextea.trade.order.infrastructure.persistence.po.CustomizationItemPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CustomizationItemMapper {

    @Select("<script>" +
            "SELECT * FROM customization_items " +
            "WHERE product_id IN " +
            "<foreach collection='productIds' item='id' open='(' separator=',' close=')'>(#{id})</foreach>" +
            "ORDER BY product_id, sort ASC, id ASC" +
            "</script>")
    List<CustomizationItemPO> selectByProductIds(@Param("productIds") List<Long> productIds);
}
