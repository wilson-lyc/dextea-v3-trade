package cn.dextea.trade.infrastructure.acl.customizationoption;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CustomizationOptionMapper {

    @Select("SELECT id, customization_id AS customizationId, name, price, sort, status, " +
            "ingredient_id AS ingredientId, ingredient_quantity AS ingredientQuantity, " +
            "created_at AS createdAt, updated_at AS updatedAt " +
            "FROM customization_options WHERE id = #{id}")
    CustomizationOptionPO selectById(@Param("id") Long id);

    @Select("<script>" +
            "SELECT id, customization_id AS customizationId, name, price, sort, status, " +
            "ingredient_id AS ingredientId, ingredient_quantity AS ingredientQuantity, " +
            "created_at AS createdAt, updated_at AS updatedAt " +
            "FROM customization_options WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>(#{id})</foreach>" +
            "</script>")
    List<CustomizationOptionPO> selectByIds(@Param("ids") List<Long> ids);
}
