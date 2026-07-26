package cn.dextea.trade.infrastructure.acl.customization;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CustomizationMapper {

    @Select("SELECT id, product_id AS productId, name, sort, status, created_at AS createdAt, updated_at AS updatedAt " +
            "FROM customizations WHERE id = #{id}")
    CustomizationPO selectById(@Param("id") Long id);

    @Select("<script>" +
            "SELECT id, product_id AS productId, name, sort, status, created_at AS createdAt, updated_at AS updatedAt " +
            "FROM customizations WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>(#{id})</foreach>" +
            "</script>")
    List<CustomizationPO> selectByIds(@Param("ids") List<Long> ids);
}
