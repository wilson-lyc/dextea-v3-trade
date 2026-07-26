package cn.dextea.trade.catalog.infrastructure.persistence;

import cn.dextea.trade.catalog.domain.model.CustomizationOption;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CustomizationOptionMapper {

    /**
     * 批量查询客制化选项
     *
     * @param ids 客制化选项ID列表
     * @return 客制化选项列表
     */
    @Select("<script>" +
            "SELECT id, customization_id AS customizationId, name, price, status " +
            "FROM customization_options " +
            "WHERE id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>(#{id})</foreach>" +
            "</script>")
    List<CustomizationOption> selectByIds(@Param("ids") List<Long> ids);
}
