package cn.dextea.trade.order.infrastructure.persistence.mapper;

import cn.dextea.trade.order.infrastructure.persistence.po.CustomizationItemPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Set;

@Mapper
public interface CustomizationItemMapper {

    @Select("<script>SELECT * FROM customization_items WHERE product_id IN "
            + "<foreach collection='productIds' item='pid' open='(' separator=',' close=')'>#{pid}</foreach></script>")
    List<CustomizationItemPO> selectByProductIds(@Param("productIds") Set<Long> productIds);
}
