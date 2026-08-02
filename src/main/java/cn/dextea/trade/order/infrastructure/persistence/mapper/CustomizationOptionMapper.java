package cn.dextea.trade.order.infrastructure.persistence.mapper;

import cn.dextea.trade.order.infrastructure.persistence.po.CustomizationOptionPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Set;

@Mapper
public interface CustomizationOptionMapper {

    @Select("<script>SELECT * FROM customization_options WHERE item_id IN "
            + "<foreach collection='itemIds' item='iid' open='(' separator=',' close=')'>#{iid}</foreach></script>")
    List<CustomizationOptionPO> selectByItemIds(@Param("itemIds") Set<Long> itemIds);
}
