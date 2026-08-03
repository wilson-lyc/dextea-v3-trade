package cn.dextea.trade.order.infrastructure.persistence.mapper;

import cn.dextea.trade.order.infrastructure.persistence.po.CustomizationOptionStoreStatusPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CustomizationOptionStoreStatusMapper {

    @Select("<script>SELECT * FROM customization_option_store_status WHERE option_id IN "
            + "<foreach collection='optionIds' item='oid' open='(' separator=',' close=')'>#{oid}</foreach> "
            + "AND store_id = #{storeId}</script>")
    List<CustomizationOptionStoreStatusPO> selectByOptionIdsAndStoreId(@Param("optionIds") java.util.Set<Long> optionIds,
                                                                       @Param("storeId") Long storeId);
}
