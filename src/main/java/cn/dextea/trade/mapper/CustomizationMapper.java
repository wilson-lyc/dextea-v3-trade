package cn.dextea.trade.mapper;

import cn.dextea.trade.entity.Customization;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CustomizationMapper {

    /**
     * 批量查询客制化项目（含名称、全局状态）。
     */
    @Select("<script>" +
            "SELECT id, product_id AS productId, name, status " +
            "FROM customizations " +
            "WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>(#{id})</foreach>" +
            "</script>")
    List<Customization> selectByIds(@Param("ids") List<Long> ids);
}
