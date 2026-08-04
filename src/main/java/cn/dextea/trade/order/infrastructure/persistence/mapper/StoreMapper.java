package cn.dextea.trade.order.infrastructure.persistence.mapper;

import cn.dextea.trade.order.infrastructure.persistence.po.StorePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Collection;
import java.util.List;

@Mapper
public interface StoreMapper {

    @Select("SELECT * FROM stores WHERE id = #{id}")
    StorePO selectById(@Param("id") Long id);

    @Select("<script>SELECT * FROM stores WHERE id IN "
            + "<foreach collection='ids' item='id' open='(' separator=',' close=')'>#{id}</foreach>"
            + "</script>")
    List<StorePO> selectByIds(@Param("ids") Collection<Long> ids);
}
