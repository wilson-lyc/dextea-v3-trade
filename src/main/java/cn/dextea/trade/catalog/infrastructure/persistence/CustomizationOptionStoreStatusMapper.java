package cn.dextea.trade.catalog.infrastructure.persistence;

import cn.dextea.trade.catalog.domain.model.CustomizationOptionStoreStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CustomizationOptionStoreStatusMapper {

    /**
     * 批量查询客制化选项门店状态
     *
     * @param optionIds 客制化选项ID列表
     * @param storeId 门店ID
     * @return 客制化选项门店状态列表
     */
    @Select("<script>" +
            "SELECT customization_option_id AS customizationOptionId, store_id AS storeId, status " +
            "FROM customization_option_store_status " +
            "WHERE store_id = #{storeId} " +
            "AND customization_option_id IN " +
            "<foreach collection='optionIds' item='id' open='(' separator=',' close=')'>(#{id})</foreach>" +
            "</script>")
    List<CustomizationOptionStoreStatus> selectByOptionIdsAndStoreId(@Param("optionIds") List<Long> optionIds,
                                                                     @Param("storeId") Long storeId);
}
