package cn.dextea.trade.order.infrastructure.persistence.mapper;

import cn.dextea.trade.order.infrastructure.persistence.po.CustomizationOptionPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CustomizationOptionMapper {

    @Select("<script>" +
            "SELECT id, item_id AS itemId, name, price, sort, status, ingredient_id AS ingredientId, " +
            "ingredient_quantity AS ingredientQuantity, created_at AS createdAt, updated_at AS updatedAt " +
            "FROM customization_options " +
            "WHERE item_id IN " +
            "<foreach collection='itemIds' item='id' open='(' separator=',' close=')'>(#{id})</foreach>" +
            "ORDER BY item_id, sort ASC, id ASC" +
            "</script>")
    List<CustomizationOptionPO> selectByItemIds(@Param("itemIds") List<Long> itemIds);
}
