package cn.dextea.trade.order.infrastructure.persistence.mapper;

import cn.dextea.trade.order.infrastructure.persistence.po.StorePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface StoreMapper {

    @Select("SELECT id, status, created_at, updated_at "
            + "FROM store WHERE id = #{id}")
    StorePO selectById(@Param("id") Long id);
}
