package cn.dextea.trade.mapper;

import cn.dextea.trade.entity.CustomizationOptionStoreStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CustomizationOptionStoreStatusMapper {

    /**
     * 批量查询指定门店下客制化选项的门店状态。采用懒加载，无记录表示禁用，由调用方处理默认值。
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
